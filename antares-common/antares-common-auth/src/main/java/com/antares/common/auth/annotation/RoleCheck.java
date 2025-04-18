package com.antares.common.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Antares
 * @date 2023/8/24 17:48
 * @description 权限校验
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RoleCheck {
    /**
     * 必须有某个角色
     * @return
     */
    String mustRole() default "";
}

