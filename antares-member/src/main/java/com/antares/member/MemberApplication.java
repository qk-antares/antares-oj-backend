package com.antares.member;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableRabbit
@EnableScheduling
public class MemberApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
    }
}
