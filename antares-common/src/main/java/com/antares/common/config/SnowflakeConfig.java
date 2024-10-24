package com.antares.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

@Configuration
public class SnowflakeConfig {

    @Bean
    public Snowflake snowflake() {
        // 配置数据中心ID和机器ID，这里设置为1和1，真实场景下应该从配置文件读取
        return IdUtil.getSnowflake(1, 1);
    }
}
