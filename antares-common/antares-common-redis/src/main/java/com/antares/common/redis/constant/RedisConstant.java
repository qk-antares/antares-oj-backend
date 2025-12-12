package com.antares.common.redis.constant;


public interface RedisConstant {
    /**
     * 验证码
     */
    String MAIL_CODE_CACHE_PREFIX = "user:mail:code:";

    /**
     * 用户TOKEN
     */
    String USER_TOKEN_PREFIX = "user:token:";

    /**
     * 签到记录
     */
    String CHECK_IN_FORMAT = "checkin:%d:%s";
}
