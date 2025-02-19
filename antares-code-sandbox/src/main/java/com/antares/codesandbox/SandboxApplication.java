package com.antares.codesandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.antares.codesandbox", "com.antares.common"})
@EnableDiscoveryClient
public class SandboxApplication {
    public static void main(String[] args) {
        SpringApplication.run(SandboxApplication.class, args);
    }
}