package com.antares.codesandbox.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.antares.codesandbox.model.dto.ExecuteCodeReq;
import com.antares.codesandbox.model.dto.ExecuteCodeRes;
import com.antares.codesandbox.service.SandboxService;
import com.antares.codesandbox.template.SandboxTemplate;
import com.antares.common.model.enums.judge.LanguageEnum;

@Service
public class SandboxServiceImpl implements SandboxService {
    @Resource
    private SandboxTemplate javaAcmSandbox;
    @Resource
    private SandboxTemplate cppAcmSandbox;

    @Override
    public ExecuteCodeRes execute(ExecuteCodeReq executeCodeReq) {
        List<String> inputList = executeCodeReq.getInputList();
        String code = executeCodeReq.getCode();
        LanguageEnum language = LanguageEnum.getEnumByValue(executeCodeReq.getLanguage());

        switch (language) {
            case LanguageEnum.JAVA:
                return javaAcmSandbox.executeCode(inputList, code, ".java");
            case LanguageEnum.CPP:
                return cppAcmSandbox.executeCode(inputList, code, ".cpp");
            default:
                return null;
        }
    }
}
