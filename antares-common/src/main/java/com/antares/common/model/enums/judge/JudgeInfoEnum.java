package com.antares.common.model.enums.judge;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * @author Antares
 * @date 2023/8/24 17:12
 * @description 判题信息消息枚举
 */
@Getter
public enum JudgeInfoEnum {
    ACCEPTED("通过！", "Accepted"),
    WRONG_ANSWER("输出结果与预期不符！", "Wrong Answer"),
    COMPILE_ERROR("未通过编译！", "Compile Error"),
    MEMORY_LIMIT_EXCEEDED("内存溢出！", "Out of Memory"),
    TIME_LIMIT_EXCEEDED("运行时间超出限制！", "Time Limit Exceeded"),
    PRESENTATION_ERROR("展示错误！", "Presentation Error"),
    WAITING("等待中！", "Waiting"),
    OUTPUT_LIMIT_EXCEEDED("输出溢出！", "Output Limit Exceeded"),
    DANGEROUS_OPERATION("危险操作！", "Dangerous Operation"),
    RUNTIME_ERROR("运行错误！", "Runtime Error"),
    SYSTEM_ERROR("系统错误！", "System Error");

    private final String text;
    private final String value;

    JudgeInfoEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static JudgeInfoEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
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
