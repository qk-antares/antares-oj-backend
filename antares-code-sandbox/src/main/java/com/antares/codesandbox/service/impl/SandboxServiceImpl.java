package com.antares.codesandbox.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.antares.codesandbox.model.dto.ExecuteCodeReq;
import com.antares.codesandbox.model.dto.ExecuteCodeRes;
import com.antares.codesandbox.service.SandboxService;
import com.antares.codesandbox.template.SandboxTemplate;

@Service
public class SandboxServiceImpl implements SandboxService {
    @Resource
    private SandboxTemplate javaAcmSandbox;
    @Resource
    private SandboxTemplate cppAcmSandbox;

    @Override
    public ExecuteCodeRes execute(ExecuteCodeReq executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        switch (language) {
            case "java":
                return javaAcmSandbox.executeCode(inputList, code, ".java");
            case "cpp":
                return cppAcmSandbox.executeCode(inputList, code, ".cpp");
            default:
                return null;
        }
    }
}
