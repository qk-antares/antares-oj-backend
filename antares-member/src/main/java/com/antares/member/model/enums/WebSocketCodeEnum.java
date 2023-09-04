package com.antares.member.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WebSocketCodeEnum {
    // 成功
    SUCCESS(200,"操作成功"),
    INTERNAL_SERVER_ERROR(505, "未知的服务器内部异常"),
    CONNECTED(10001, "已经连接了，请先退出"),
    PARAMS_ERROR(401, "请求参数不合法"),
    NOT_EXIST(404, "指令不存在");


    private final int code;
    private final String msg;

    public static WebSocketCodeEnum getEnumByCode(int code) {
        for (WebSocketCodeEnum webSocketCodeEnum : WebSocketCodeEnum.values()) {
            if (webSocketCodeEnum.getCode() == code) {
                return webSocketCodeEnum;
            }
        }
        return null;
    }
}
