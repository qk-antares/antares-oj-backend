package com.antares.user.model.enums;

import lombok.Getter;

/**
 * @author Antares
 * @date 2023/8/24 17:12
 * @description 用户角色枚举
 */
@Getter
public enum UserRoleEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin"),
    BAN("被封号", "ban");

    private final String text;

    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }
}
