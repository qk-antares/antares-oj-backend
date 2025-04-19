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

import com.antares.common.auth.constant.UserConstant;
import com.antares.common.core.enums.HttpCodeEnum;
import com.antares.common.core.exception.BusinessException;
import com.antares.common.redis.constant.RedisConstant;
import com.antares.user.mapper.UserMapper;
import com.antares.user.model.dto.AccountLoginReq;
import com.antares.user.model.dto.CodeLoginReq;
import com.antares.user.model.entity.User;
import com.antares.user.service.LoginService;
import com.antares.user.utils.MailUtil;
import com.antares.user.utils.UidUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

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
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MailUtil mailUtil;
    @Resource
    private Snowflake snowflake;
    @Value("${antares.domain}")
    private String domain;
    @Value("${antares.user.secret-key}")
    private String secretKey;
    @Value("${antares.user.token-expire-hours}")
    private Integer tokenExpireHours;

    @Override
    public void sendMailCode(String email) {
        String redisCodeKey = RedisConstant.MAIL_CODE_CACHE_PREFIX + email;
        String redisCode = stringRedisTemplate.opsForValue().get(redisCodeKey);
        // 1、接口防刷
        // 发送过验证码了
        if (!StrUtil.isEmpty(redisCode)) {
            // 用当前时间减去存入redis的时间，判断用户邮箱是否在60s内发送过验证码
            long currentTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - currentTime < 60000) {
                // 60s内不能再发
                throw new BusinessException(HttpCodeEnum.CODE_EXCEPTION);
            }
        }

        // 2、存入redis，value是"codeNum_系统时间"
        int code = (int) ((Math.random() * 9 + 1) * 100000);
        String codeNum = String.valueOf(code);
        String redisStorage = codeNum + "_" + System.currentTimeMillis();
        stringRedisTemplate.opsForValue().set(redisCodeKey, redisStorage, 10, TimeUnit.MINUTES);

        // 3、异步发送验证码
        mailUtil.sendMail(email, codeNum);

        log.info("向email{}发送验证码: {}", email, code);
    }

    @Override
    public void loginByAccount(AccountLoginReq req, HttpServletResponse res) {
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

    @Override
    public void loginByCode(CodeLoginReq req, HttpServletResponse res) {
        // 到这里参数校验已经通过
        String email = req.getEmail();
        String captcha = req.getCaptcha();
        // 从redis中读取验证码
        String cacheKey = RedisConstant.MAIL_CODE_CACHE_PREFIX + email;
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
                register.setUsername(UidUtil.snowflakeUidToString(uid));

                // 设置AK/SK
                register.setSecretId(DigestUtil.sha1Hex(uid + RandomUtil.randomString(32)));
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
                .set(RedisConstant.USER_TOKEN_PREFIX + user.getUid(), token, tokenExpireHours, TimeUnit.HOURS);

        // 设置cookie
        Cookie cookie = new Cookie(UserConstant.TOKEN, token);
        cookie.setDomain(domain);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(tokenExpireHours * 60 * 60);
        res.addCookie(cookie);
    }
}
