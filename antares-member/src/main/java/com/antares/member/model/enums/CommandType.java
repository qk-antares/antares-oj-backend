package com.antares.member.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandType {
    CONNECTION(10001),
    CHAT(10002),
    CHANGE(10003),
    ERROR(-1);

    private final Integer code;

    public static CommandType match(Integer code){
        for (CommandType value : CommandType.values()) {
            if(value.getCode().equals(code)){
                return value;
            }
        }
        return ERROR;
    }
}
