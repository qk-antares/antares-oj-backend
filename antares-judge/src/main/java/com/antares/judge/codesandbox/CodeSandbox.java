package com.antares.judge.codesandbox;

import com.antares.codesandbox.sdk.model.dto.ExecuteCodeReq;
import com.antares.codesandbox.sdk.model.dto.ExecuteCodeRes;

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
    ExecuteCodeRes executeCode(ExecuteCodeReq executeCodeRequest, String accessKey, String secretKey);
}
