package com.antares.common.model.enums.judge;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * @author Antares
 * @date 2023/8/24 17:12
 * @description 题目提交编程语言枚举
 */
@Getter
public enum LanguageEnum {

    JAVA("Java"),
    CPP("C++"),
    GOLANG("JavaScript"),
    PYTHON("Python");

    private final String value;

    LanguageEnum(String value) {
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static LanguageEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (LanguageEnum anEnum : LanguageEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
