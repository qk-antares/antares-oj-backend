package com.antares.common.core.enums;

import lombok.Getter;

@Getter
public enum HttpCodeEnum {
    // 成功
    SUCCESS(200,"操作成功"),
    //服务器内部异常
    INTERNAL_SERVER_ERROR(505, "未知的服务器内部异常"),
    //参数不合法
    BAD_REQUEST(400, "请求参数不合法"),
    //无权限
    NO_AUTH(403, "没有执行操作的权限"),
    //请求的资源不存在
    NOT_EXIST(404, "请求的资源不存在"),
    // 登录
    CODE_EXCEPTION(10001,"验证码获取频率太高，请稍后再试"),
    WRONG_CODE(10002, "验证码错误"),
    ACCOUNT_NOT_EXIST(10003, "账号不存在"),
    WRONG_PASSWORD(10004, "账号或密码错误"),
    THIRD_PARTY_EXCEPTION(10005, "第三方登录异常"),
    NOT_LOGIN(10006, "未登录"),
    //注册
    PHONE_EXIST(10011, "手机号已注册"),
    EMAIL_EXIST(10012, "邮箱已注册"),
    USER_EXIST(10013, "用户已存在"),
    //用户属性
    USER_TAG_EXIST(10021, "该标签已存在"),
    USERNAME_EXIST(10022, "用户名已存在"),
    //文章标签
    ARTICLE_TAG_EXIST(11031, "该标签已存在"),

    //OJ相关
    SUBMIT_ERROR(20001, "提交判题失败");


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
