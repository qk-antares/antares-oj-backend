package com.antares.common.model.enums.judge;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

@Getter
public enum ProblemStatusEnum {
    ALL("全部"),
    SOLVED("已通过"),
    TRIED("尝试过"),
    NOLOG("未开始");

    private final String value;
    
    ProblemStatusEnum(String value) {
        this.value = value;
    }

    /**
     * 获取值列表
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     * @param value
     * @return
     */
    public static ProblemStatusEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (ProblemStatusEnum anEnum : ProblemStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
