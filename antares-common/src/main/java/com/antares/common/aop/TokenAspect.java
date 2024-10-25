package com.antares.common.aop;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.antares.common.constant.RedisConstants;
import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.HttpCodeEnum;

import cn.hutool.core.convert.NumberWithFormat;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTException;

@Component
@Aspect
@Order(1)
public class TokenAspect {
    @Value("${antares.user.secret-key}")
    private String secretKey;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Before("@annotation(com.antares.common.annotation.TokenCheck)")
    public void doBefore() {
        // 获取当前请求的HTTP请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 从请求头中获取Token
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BusinessException(HttpCodeEnum.NO_AUTH, "TOKEN不存在或格式错误");
        }

        token = token.substring(7); // 去掉 "Bearer " 前缀

        // 验证Token是否有效
        try {
            boolean validate = JWT.of(token).setKey(secretKey.getBytes()).validate(0L);
            if (!validate) {
                throw new BusinessException(HttpCodeEnum.NO_AUTH, "无效的TOKEN");
            }
        } catch (JWTException e) {
            throw new BusinessException(HttpCodeEnum.NO_AUTH, "TOKEN验证失败");
        }

        // 验证Token是否和Redis中一致
        Long uid = (Long) ((NumberWithFormat) JWT.of(token).getPayload("uid")).getNumber();
        String redisToken = stringRedisTemplate.opsForValue().get(RedisConstants.USER_TOKEN_PREFIX + uid);
        if (!token.equals(redisToken)) {
            throw new BusinessException(HttpCodeEnum.NO_AUTH, "无效的TOKEN");
        }
    }
}
