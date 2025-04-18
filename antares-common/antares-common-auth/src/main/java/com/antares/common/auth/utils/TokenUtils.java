package com.antares.common.auth.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.antares.common.auth.constant.UserConstant;

import cn.hutool.core.convert.NumberWithFormat;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;

public class TokenUtils {
    public static String getToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 获取所有的Cookie
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            // 遍历所有的Cookie，找到name为TOKEN的Cookie
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(UserConstant.TOKEN)) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        return token;
    }

    public static Long getCurrentUid() {
        String token = getToken();
        if (StrUtil.isBlank(token)) {
            return null;
        }

        try {
            Long uid = (Long) ((NumberWithFormat) JWT.of(token).getPayload("uid")).getNumber();
            return uid;
        } catch (Exception e) {
            return null;
        }
    }
}
