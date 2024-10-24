package com.antares.judge.codesandbox.impl;

import org.springframework.stereotype.Service;

import com.antares.codesandbox.sdk.model.dto.executecode.ExecuteCodeRequest;
import com.antares.codesandbox.sdk.model.dto.executecode.ExecuteCodeResponse;
import com.antares.codesandbox.sdk.model.enums.ExecuteCodeStatusEnum;
import com.antares.judge.codesandbox.CodeSandbox;

import lombok.extern.slf4j.Slf4j;

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
