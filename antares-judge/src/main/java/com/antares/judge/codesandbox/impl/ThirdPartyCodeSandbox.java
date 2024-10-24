package com.antares.judge.codesandbox.impl;

import org.springframework.stereotype.Service;

import com.antares.codesandbox.sdk.model.dto.executecode.ExecuteCodeRequest;
import com.antares.codesandbox.sdk.model.dto.executecode.ExecuteCodeResponse;
import com.antares.judge.codesandbox.CodeSandbox;

/**
 * 第三方代码沙箱（调用网上现成的代码沙箱）
 */
@Service
public class ThirdPartyCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest, String accessKey, String secretKey) {
        System.out.println("第三方代码沙箱");
        return null;
    }
}
