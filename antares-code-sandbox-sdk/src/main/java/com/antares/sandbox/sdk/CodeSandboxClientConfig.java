package com.antares.sandbox.sdk;

import com.antares.sandbox.sdk.client.CodeSandboxClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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
