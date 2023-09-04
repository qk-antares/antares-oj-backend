package com.antares.oj.codesandbox;

import com.antares.sandbox.sdk.model.dto.executecode.ExecuteCodeRequest;
import com.antares.sandbox.sdk.model.dto.executecode.ExecuteCodeResponse;

/**
 * @author Antares
 * @date 2023/9/3 11:20
 * @description 代码沙箱接口定义
 */
public interface CodeSandbox {

    /**
     * 带AK/SK的
     * @param executeCodeRequest
     * @param accessKey
     * @param secretKey
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest, String accessKey, String secretKey);
}
