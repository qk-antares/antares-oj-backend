package com.antares.sandbox.service.impl;

import lombok.extern.slf4j.Slf4j;
import com.antares.sandbox.model.dto.ExecuteCodeRequest;
import com.antares.sandbox.model.dto.ExecuteCodeResponse;
import com.antares.sandbox.service.SandboxService;
import com.antares.sandbox.template.cpp.CppSandboxTemplate;
import com.antares.sandbox.template.java.JavaSandboxTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
