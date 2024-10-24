package com.antares.common.utils;

import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.HttpCodeEnum;

/**
 * 抛异常工具类
 */
public class ThrowUtils {

    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param runtimeException
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param appHttpCodeEnum
     */
    public static void throwIf(boolean condition, HttpCodeEnum appHttpCodeEnum) {
        throwIf(condition, new BusinessException(appHttpCodeEnum));
    }

    /**
     * 条件成立则抛异常
     * @param condition
     * @param appHttpCodeEnum
     * @param msg
     */
    public static void throwIf(boolean condition, HttpCodeEnum appHttpCodeEnum, String msg) {
        throwIf(condition, new BusinessException(appHttpCodeEnum, msg));
    }
}
