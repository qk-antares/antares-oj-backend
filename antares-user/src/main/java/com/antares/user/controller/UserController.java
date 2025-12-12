package com.antares.user.controller;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antares.common.auth.annotation.TokenCheck;
import com.antares.common.auth.utils.TokenUtils;
import com.antares.common.core.dto.R;
import com.antares.common.redis.constant.RedisConstant;
import com.antares.user.mapper.UserMapper;
import com.antares.user.model.entity.User;
import com.antares.user.model.vo.AKSKVo;
import com.antares.user.model.vo.UserVo;

import cn.hutool.core.convert.NumberWithFormat;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping
public class UserController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserMapper userMapper;
    @Value("${antares.user.secret-key}")
    private String secretKey;

    @GetMapping(value = "/current")
    public R<UserVo> getCurrentUser() {
        // 从Cookie中获取Token
        String token = TokenUtils.getToken();

        if (token == null) {
            return R.ok(null);
        }

        // 验证Token是否有效
        JWT jwt;
        try {
            jwt = JWT.of(token).setKey(secretKey.getBytes());
            if (!jwt.validate(0L)) {
                return R.ok(null);
            }
        } catch (JWTException e) {
            return R.ok(null);
        }

        // 验证Token是否和Redis中一致
        Long uid = (Long) ((NumberWithFormat) jwt.getPayload("uid")).getNumber();
        String redisToken = stringRedisTemplate.opsForValue().get(RedisConstant.USER_TOKEN_PREFIX + uid);
        if (!token.equals(redisToken)) {
            return R.ok(null);
        }

        User user = userMapper.selectById(uid);
        return R.ok(UserVo.userToVo(user));
    }

    @GetMapping(value = "/aksk")
    @TokenCheck
    public R<AKSKVo> getAKSK() {
        Long uid = TokenUtils.getCurrentUid();
        User user = userMapper.selectById(uid);
        AKSKVo akskVo = new AKSKVo();
        akskVo.setAccessKey(user.getSecretId());
        akskVo.setSecretKey(user.getSecretKey());
        return R.ok(akskVo);
    }

    @PostMapping(value = "/logout")
    @TokenCheck
    public R<Void> logout() {
        Long uid = TokenUtils.getCurrentUid();
        stringRedisTemplate.delete(RedisConstant.USER_TOKEN_PREFIX + uid);
        return R.ok();
    }
}
