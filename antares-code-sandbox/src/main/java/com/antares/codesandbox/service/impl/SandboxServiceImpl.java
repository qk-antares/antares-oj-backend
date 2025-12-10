package com.antares.codesandbox.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.antares.codesandbox.model.dto.ExecuteCodeReq;
import com.antares.codesandbox.model.dto.ExecuteCodeRes;
import com.antares.codesandbox.model.enums.ExecuteCodeStatusEnum;
import com.antares.codesandbox.model.enums.LanguageEnum;
import com.antares.codesandbox.service.SandboxService;
import com.antares.codesandbox.template.SandboxTemplate;

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

        if(language == null) {
            return ExecuteCodeRes.builder()
                    .code(ExecuteCodeStatusEnum.NOT_SUPPORTED_LANGUAGE.getValue())
                    .msg(ExecuteCodeStatusEnum.NOT_SUPPORTED_LANGUAGE.getMsg())
                    .build();
        }

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
