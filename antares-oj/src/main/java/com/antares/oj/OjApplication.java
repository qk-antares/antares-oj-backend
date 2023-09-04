package com.antares.oj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.antares.oj.feign")
public class OjApplication {
    public static void main(String[] args) {
        SpringApplication.run(OjApplication.class, args);
    }
}