package com.antares.oj.codesandbox.impl;

import com.antares.sandbox.sdk.model.dto.executecode.ExecuteCodeRequest;
import com.antares.sandbox.sdk.model.dto.executecode.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import com.antares.oj.codesandbox.CodeSandbox;
import com.antares.oj.model.enums.ExecuteCodeStatusEnum;
import org.springframework.stereotype.Service;

/**
 * 示例代码沙箱（仅为了跑通业务流程）
 */
@Slf4j
@Service
public class ExampleCodeSandbox implements CodeSandbox {
    /**
     * 示例代码沙箱，假设执行总是成功
     * @param executeCodeRequest
     * @return
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest, String accessKey, String secretKey) {
        ExecuteCodeResponse response = new ExecuteCodeResponse();
        response.setCode(ExecuteCodeStatusEnum.SUCCESS.getValue());
        response.setMsg(ExecuteCodeStatusEnum.SUCCESS.getText());
        return response;
    }
}
