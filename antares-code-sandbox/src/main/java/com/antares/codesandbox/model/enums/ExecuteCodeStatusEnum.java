package com.antares.codesandbox.model.enums;

import lombok.Getter;

/*
 * 所有测试用例总的执行状态
 */
@Getter
public enum ExecuteCodeStatusEnum {
    NOT_SUPPORTED_LANGUAGE("不支持的语言类型", 4),
    TIMEOUT("超出时间限制", 3),
    RUN_FAILED("执行出错", 2),
    COMPILE_FAILED("编译失败", 1),
    SUCCESS("成功", 0);

    private final String msg;

    private final Integer value;

    ExecuteCodeStatusEnum(String msg, Integer value) {
        this.msg = msg;
        this.value = value;
    }

    public static ExecuteCodeStatusEnum getEnumByExitCodeEnum(ExitCodeEnum exitCodeEnum) {
        switch (exitCodeEnum) {
            case ExitCodeEnum.SUCCESS:
                return SUCCESS;
            case ExitCodeEnum.RUN_FAILED:
                return RUN_FAILED;
            case ExitCodeEnum.TIMEOUT:
                return TIMEOUT;
            default:
                return null;
        }
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static ExecuteCodeStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ExecuteCodeStatusEnum anEnum : ExecuteCodeStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
