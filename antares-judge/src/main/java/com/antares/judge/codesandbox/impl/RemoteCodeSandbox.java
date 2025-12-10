package com.antares.judge.codesandbox.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.antares.codesandbox.sdk.client.CodeSandboxClient;
import com.antares.codesandbox.sdk.model.dto.ExecuteCodeReq;
import com.antares.codesandbox.sdk.model.dto.ExecuteCodeRes;
import com.antares.judge.codesandbox.CodeSandbox;

import lombok.extern.slf4j.Slf4j;

/**
 * 远程代码沙箱（实际调用接口的沙箱）
 */
@Service
@Slf4j
public class RemoteCodeSandbox implements CodeSandbox {
    @Value("${antares.code-sandbox.gateway-host:#{null}}")
    private String gatewayHost;

    @Override
    public ExecuteCodeRes executeCode(ExecuteCodeReq executeCodeRequest, String accessKey, String secretKey) {
        log.info("accessKey:{}, secretKey:{}", accessKey, secretKey);
        CodeSandboxClient client;
        client = new CodeSandboxClient(accessKey, secretKey, gatewayHost);
        return client.executeCode(executeCodeRequest);
    }
}
