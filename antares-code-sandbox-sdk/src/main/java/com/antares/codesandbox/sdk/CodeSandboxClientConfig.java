package com.antares.codesandbox.sdk;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.antares.codesandbox.sdk.client.CodeSandboxClient;
import com.antares.codesandbox.sdk.constant.UrlConstant;

import lombok.Data;

@Configuration
@ConfigurationProperties("antares.code-sandbox")
@Data
@ComponentScan
public class CodeSandboxClientConfig {
    private String accessKey;
    private String secretKey;
    private String gatewayHost = UrlConstant.GATEWAY_HOST;

    @Bean
    public CodeSandboxClient codeSandboxClient() {
        return new CodeSandboxClient(accessKey, secretKey, gatewayHost);
    }
}
