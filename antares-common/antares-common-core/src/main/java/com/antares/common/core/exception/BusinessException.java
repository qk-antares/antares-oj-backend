package com.antares.common.core.exception;

import com.antares.common.core.enums.HttpCodeEnum;

import lombok.Getter;

/**
 * @author Antares
 */
@Getter
public class BusinessException extends RuntimeException{
    private int code;

    public BusinessException(HttpCodeEnum httpCodeEnum) {
        super(httpCodeEnum.getMsg());
        this.code = httpCodeEnum.getCode();
    }

    public BusinessException(HttpCodeEnum httpCodeEnum, String msg) {
        super(msg);
        this.code = httpCodeEnum.getCode();
    }
}