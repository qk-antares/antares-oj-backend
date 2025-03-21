package com.antares.codesandbox.sdk.model.enums;

import lombok.Getter;

/*
 * 单个测试用例的执行状态
 */
@Getter
public enum ExitCodeEnum {
    TIMEOUT(-2),
    RUN_FAILED(-1),
    SUCCESS(0);

    private final Integer value;

    ExitCodeEnum(Integer value) {
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static ExitCodeEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ExitCodeEnum anEnum : ExitCodeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
