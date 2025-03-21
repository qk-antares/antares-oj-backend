package com.antares.common.model.enums.judge;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

@Getter
public enum ProblemDifficultyEnum {
    EASY("简单"),
    MEDIUM("中等"),
    HARD("困难");

    private final String value;

    ProblemDifficultyEnum(String value) {
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     * @param value
     * @return
     */
    public static ProblemDifficultyEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (ProblemDifficultyEnum anEnum : ProblemDifficultyEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
