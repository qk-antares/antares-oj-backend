package com.antares.user.service.impl;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.antares.common.constant.RedisConstants;
import com.antares.common.constant.UserConstant;
import com.antares.common.exception.BusinessException;
import com.antares.common.mapper.UserMapper;
import com.antares.common.model.dto.user.AccountLoginRequest;
import com.antares.common.model.dto.user.CodeLoginReq;
import com.antares.common.model.entity.User;
import com.antares.common.model.enums.HttpCodeEnum;
import com.antares.user.service.LoginService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.convert.NumberWithFormat;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.jwt.JWT;
import lombok.extern.slf4j.Slf4j;

@Service
@RefreshScope
@Slf4j
public class LoginServiceImpl extends ServiceImpl<UserMapper, User> implements LoginService {
    @Resource
    private Snowflake snowflake;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Value("${antares.domain}")
    private String domain;
    @Value("${antares.user.secret-key}")
    private String secretKey;
    @Value("${antares.user.token-expire-hours}")
    private Integer tokenExpireHours;

    @Override
    public void loginByAccount(AccountLoginRequest req, HttpServletResponse res) {
        String email = req.getEmail();
        String password = req.getPassword();

        // 1、去数据库查询 SELECT * FROM user WHERE email = ?
        User user = baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));

        if (user != null) {
            // 2、判断密码是否正确
            // 获取到数据库里的password
            String passwordCrypt = user.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            // 进行密码匹配
            boolean matches = passwordEncoder.matches(password, passwordCrypt);
            if (matches) {
                // 登录成功
                refreshToken(user, res);
            } else {
                throw new BusinessException(HttpCodeEnum.WRONG_PASSWORD);
            }
        } else {
            // 登录失败
            throw new BusinessException(HttpCodeEnum.ACCOUNT_NOT_EXIST);
        }
    }

    private void refreshToken(User user, HttpServletResponse res) {
        // 密钥
        byte[] key = secretKey.getBytes();
        // 过期时间
        Date expireDate = new Date(System.currentTimeMillis() + tokenExpireHours * 60 * 60 * 1000);

        String token = JWT.create()
                .setPayload("uid", user.getUid())
                .setPayload("userRole", user.getUserRole())
                .setExpiresAt(expireDate)
                .setKey(key)
                .sign();

        stringRedisTemplate.opsForValue()
                .set(RedisConstants.USER_TOKEN_PREFIX + user.getUid(), token, tokenExpireHours, TimeUnit.HOURS);

        //设置cookie
        Cookie cookie = new Cookie(UserConstant.TOKEN, token);
        cookie.setDomain(domain);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(tokenExpireHours * 60 * 60);
        res.addCookie(cookie);
    }

    @Override
    public void loginByCode(CodeLoginReq req, HttpServletResponse res) {
        // 到这里参数校验已经通过
        String email = req.getEmail();
        String captcha = req.getCaptcha();
        // 从redis中读取验证码
        String cacheKey = RedisConstants.MAIL_CODE_CACHE_PREFIX + email;
        String cacheCode = stringRedisTemplate.opsForValue().get(cacheKey);
        // 验证码校验通过
        if (!StrUtil.isEmpty(cacheCode) && captcha.equals(cacheCode.split("_")[0])) {
            // 删除redis中的验证码
            stringRedisTemplate.delete(cacheKey);
            // 邮箱如果不存在，则自动注册
            User result = lambdaQuery().eq(User::getEmail, email).one();
            if (result == null) {
                User register = new User();

                // 生成雪花ID
                long uid = snowflake.nextId();
                register.setUid(uid);
                register.setEmail(email);

                // 设置AK/SK
                register.setAccessKey(DigestUtil.sha1Hex(uid + RandomUtil.randomString(32)));
                register.setSecretKey(DigestUtil.sha1Hex(uid + RandomUtil.randomString(32)));
                register.setUserRole("user");

                save(register);

                refreshToken(register, res);
            } else {
                // 手机号已经存在，则正常登录
                refreshToken(result, res);
            }
        } else {
            throw new BusinessException(HttpCodeEnum.WRONG_CODE);
        }
    }

    @Override
    public void logout(String token) {
        JWT jwt = JWT.of(token.substring(7));
        Long uid = (Long) ((NumberWithFormat) jwt.getPayload("uid")).getNumber();
        stringRedisTemplate.delete(RedisConstants.USER_TOKEN_PREFIX + uid);
    }
}
