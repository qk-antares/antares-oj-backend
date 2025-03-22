package com.antares.common.model.enums.judge;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

/**
 * @author Antares
 * @date 2023/8/24 17:12
 * @description 判题信息消息枚举
 */
@Getter
public enum JudgeInfoEnum {
    ACCEPTED("通过", 0),
    COMPILE_ERROR("编译失败", 1),
    RUNTIME_ERROR("执行出错", 2),
    TIMEOUT("超出时间限制", 3),
    WRONG_ANSWER("解答错误", 4);

    private final String msg;
    private final Integer value;

    JudgeInfoEnum(String msg, Integer value) {
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
    public static JudgeInfoEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (JudgeInfoEnum anEnum : JudgeInfoEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
