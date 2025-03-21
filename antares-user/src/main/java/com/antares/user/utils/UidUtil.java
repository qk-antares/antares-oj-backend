package com.antares.user.utils;

import java.util.Base64;

public class UidUtil {
    public static String snowflakeUidToString(long uid) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (uid >> (56 - i * 8));
        }

        // 使用Base64进行编码
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
