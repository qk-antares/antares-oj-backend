package com.antares.codesandbox.sdk;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.antares.codesandbox.sdk.client.CodeSandboxClient;

import lombok.Data;

@Configuration
@ConfigurationProperties("antares.sandbox")
@Data
@ComponentScan
public class CodeSandboxClientConfig {
    private String accessKey;
    private String secretKey;

    @Bean
    public CodeSandboxClient codeSandboxClient() {
        return new CodeSandboxClient(accessKey, secretKey);
    }
}
