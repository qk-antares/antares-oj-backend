package com.antares.common.core.enums;

import lombok.Getter;

@Getter
public enum HttpCodeEnum {
    // 通用
    SUCCESS(200,"操作成功"),
    INTERNAL_SERVER_ERROR(505, "未知的服务器内部异常"),
    BAD_REQUEST(400, "请求参数不合法"),
    NO_AUTH(403, "没有执行操作的权限"),
    NOT_EXIST(404, "请求的资源不存在"),

    // 用户
    CODE_EXCEPTION(10001,"验证码获取频率太高，请稍后再试"),
    WRONG_CODE(10002, "验证码错误"),
    ACCOUNT_NOT_EXIST(10003, "账号不存在"),
    WRONG_PASSWORD(10004, "账号或密码错误"),
    NOT_LOGIN(10005, "未登录"),

    // 判题
    TOO_MANY_REQUESTS(20001, "操作过于频繁，请稍后再试");

    public int code;
    public String msg;

    HttpCodeEnum(int code, String errorMessage){
        this.code = code;
        this.msg = errorMessage;
    }

    public static HttpCodeEnum getEnumByCode(int code) {
        for (HttpCodeEnum appHttpCodeEnum : HttpCodeEnum.values()) {
            if (appHttpCodeEnum.code == code) {
                return appHttpCodeEnum;
            }
        }
        return HttpCodeEnum.INTERNAL_SERVER_ERROR;
    }
}
