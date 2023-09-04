package com.antares.sandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.antares.sandbox.feign")
public class SandboxApplication {
    public static void main(String[] args) {
        SpringApplication.run(SandboxApplication.class, args);
    }
}