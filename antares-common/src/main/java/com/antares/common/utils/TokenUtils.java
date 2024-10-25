package com.antares.common.utils;

import cn.hutool.core.convert.NumberWithFormat;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;

public class TokenUtils {
    public static Long getUidFromToken(String token) {
        if (StrUtil.isBlank(token)) {
            return null;
        }
        JWT jwt = JWT.of(token.substring(7));
        Long uid = (Long) ((NumberWithFormat) jwt.getPayload("uid")).getNumber();
        return uid;
    }
}
