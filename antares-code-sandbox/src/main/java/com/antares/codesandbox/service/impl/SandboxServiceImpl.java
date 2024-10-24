package com.antares.codesandbox.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.antares.codesandbox.model.dto.ExecuteCodeRequest;
import com.antares.codesandbox.model.dto.ExecuteCodeResponse;
import com.antares.codesandbox.service.SandboxService;
import com.antares.codesandbox.template.cpp.CppSandboxTemplate;
import com.antares.codesandbox.template.java.JavaSandboxTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SandboxServiceImpl implements SandboxService {
    @Resource
    private JavaSandboxTemplate javaNativeAcmSandbox;
    @Resource
    private CppSandboxTemplate cppNativeAcmSandbox;

    @Override
    public ExecuteCodeResponse execute(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        switch (language){
            case "java":
                return javaNativeAcmSandbox.executeJavaCode(inputList, code);
            case "cpp":
                return cppNativeAcmSandbox.executeCppCode(inputList, code);
            default:
                return null;
        }
    }
}
