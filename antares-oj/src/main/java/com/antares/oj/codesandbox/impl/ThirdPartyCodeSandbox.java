package com.antares.oj.codesandbox.impl;

import com.antares.oj.codesandbox.CodeSandbox;
import com.antares.sandbox.sdk.model.dto.executecode.ExecuteCodeRequest;
import com.antares.sandbox.sdk.model.dto.executecode.ExecuteCodeResponse;
import org.springframework.stereotype.Service;

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
