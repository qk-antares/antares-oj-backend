package com.antares.judge;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.antares.judge", "com.antares.common"})
@EnableDubbo
@EnableDiscoveryClient
@EnableCaching
@MapperScan("com.antares.judge.mapper")
@EnableAsync
public class JudgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(JudgeApplication.class, args);
    }
}