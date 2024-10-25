package com.antares.user.service;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTHeader;

public class JwtTest {
    public static void main(String[] args) {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjE4NDg2NTMxNDk4MTI5NTMwODgsInVzZXJSb2xlIjoidXNlciIsImV4cCI6MTcyOTYwMTUzN30.bXmZB0LO5R-B1HhWp86KY56dYGYoN3y0krtI7fHvcN0";

        JWT jwt = JWT.of(token);

        // JWT
        jwt.getHeader(JWTHeader.TYPE);
        // HS256
        jwt.getHeader(JWTHeader.ALGORITHM);

        // 1234567890
        System.out.println(jwt.getPayload("uid"));
    }
}
