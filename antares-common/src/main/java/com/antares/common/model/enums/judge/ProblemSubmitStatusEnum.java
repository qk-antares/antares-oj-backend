package com.antares.common.model.enums.judge;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * @author Antares
 * @date 2023/8/24 17:10
 * @description 题目提交枚举
 */
@Getter
public enum ProblemSubmitStatusEnum {
    // 0 - 等待中、1 - 判题中、2 - 解答错误、3 - 通过
    WAITING("等待中", 0),
    RUNNING("判题中", 1),
    FAILED("解答错误", 2),
    SUCCEED("通过", 3);

    private final String text;
    private final Integer value;

    ProblemSubmitStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     * @param value
     * @return
     */
    public static ProblemSubmitStatusEnum getEnumByValue(Integer value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (ProblemSubmitStatusEnum anEnum : ProblemSubmitStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
