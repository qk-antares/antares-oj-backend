package com.antares.codesandbox.sdk.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

@Getter
public enum ExecuteCodeStatusEnum {
    RUN_FAILED("运行失败", 2),
    COMPILE_FAILED("编译失败", 1),
    SUCCESS("成功", 0);

    private final String msg;

    private final Integer value;

    ExecuteCodeStatusEnum(String msg, Integer value) {
        this.msg = msg;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
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
