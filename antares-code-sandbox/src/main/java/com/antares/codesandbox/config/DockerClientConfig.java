package com.antares.codesandbox.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

@Configuration
@RefreshScope
public class DockerClientConfig {
    @Value("${antares.code-sandbox.docker-host:tcp://172.17.0.1:2375}")
    private String dockerHost;
    @Value("${antares.code-sandbox.max-connections:100}")
    private int maxConnections;
    @Value("${antares.code-sandbox.connect-timeout:30}")
    private int connectTimeout;
    @Value("${antares.code-sandbox.response-timeout:30}")
    private int responseTimeout;

    @Bean
    public DockerClient dockerClient() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(maxConnections)
                .connectionTimeout(Duration.ofSeconds(connectTimeout))
                .responseTimeout(Duration.ofSeconds(responseTimeout))
                .build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

        return dockerClient;
    }
}
