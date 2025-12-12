package com.antares.judge.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * 限流key前缀
     */
    String key() default "";
    
    /**
     * 时间窗口（秒）
     */
    int rateInterval() default 60;
    
    /**
     * 限流提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
}

