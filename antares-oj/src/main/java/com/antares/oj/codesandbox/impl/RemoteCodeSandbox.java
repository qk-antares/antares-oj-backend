package com.antares.oj.codesandbox.impl;

import com.antares.oj.codesandbox.CodeSandbox;
import com.antares.sandbox.sdk.client.CodeSandboxClient;
import com.antares.sandbox.sdk.model.dto.executecode.ExecuteCodeRequest;
import com.antares.sandbox.sdk.model.dto.executecode.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 远程代码沙箱（实际调用接口的沙箱）
 */
@Service
@Slf4j
public class RemoteCodeSandbox implements CodeSandbox {

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest, String accessKey, String secretKey) {
        log.info("accessKey:{}, secretKey:{}", accessKey, secretKey);
        CodeSandboxClient client = new CodeSandboxClient(accessKey, secretKey);
        return client.executeCode(executeCodeRequest);
    }
}
