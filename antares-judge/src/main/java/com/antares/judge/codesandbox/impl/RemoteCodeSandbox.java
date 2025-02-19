package com.antares.judge.codesandbox.impl;

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

    @Override
    public ExecuteCodeRes executeCode(ExecuteCodeReq executeCodeRequest, String accessKey, String secretKey) {
        log.info("accessKey:{}, secretKey:{}", accessKey, secretKey);
        CodeSandboxClient client = new CodeSandboxClient(accessKey, secretKey);
        return client.executeCode(executeCodeRequest);
    }
}
