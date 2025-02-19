package com.antares.codesandbox.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.antares.codesandbox.factory.SandboxFactory;
import com.antares.codesandbox.model.dto.ExecuteCodeReq;
import com.antares.codesandbox.model.dto.ExecuteCodeRes;
import com.antares.codesandbox.service.SandboxService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RefreshScope
public class SandboxServiceImpl implements SandboxService {
    @Resource
    private SandboxFactory javaSandboxFactory;
    @Resource
    private SandboxFactory cppSandboxFactory;
    @Value("${antares.code-sandbox.type:docker}")
    private String sandboxType;

    @Override
    public ExecuteCodeRes execute(ExecuteCodeReq executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        switch (language) {
            case "java":
                return javaSandboxFactory.createSandboxTemplate(sandboxType).executeCode(inputList, code, ".java");
            case "cpp":
                return cppSandboxFactory.createSandboxTemplate(sandboxType).executeCode(inputList, code, ".cpp");
            default:
                return null;
        }
    }
}
