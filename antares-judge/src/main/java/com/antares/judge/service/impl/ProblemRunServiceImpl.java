package com.antares.judge.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.antares.codesandbox.sdk.model.dto.ExecuteCodeReq;
import com.antares.codesandbox.sdk.model.dto.ExecuteCodeRes;
import com.antares.common.auth.utils.TokenUtils;
import com.antares.judge.codesandbox.CodeSandbox;
import com.antares.judge.codesandbox.CodeSandboxFactory;
import com.antares.judge.service.ProblemRunService;
import com.antares.user.api.dto.SecretDTO;
import com.antares.user.api.service.UserInnerService;

@Service
public class ProblemRunServiceImpl implements ProblemRunService {
    @Resource
    private CodeSandboxFactory codeSandboxFactory;
    @Value("${antares.code-sandbox.type:remote}")
    private String type;
    @DubboReference
    private UserInnerService userInnerService;

    @Override
    public ExecuteCodeRes doProblemRun(ExecuteCodeReq request) {
        String code = request.getCode();
        String language = request.getLanguage();
        List<String> inputList = request.getInputList();

        CodeSandbox codeSandbox = codeSandboxFactory.newInstance(type);
        ExecuteCodeReq executeCodeRequest = ExecuteCodeReq.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();

        Long userId = TokenUtils.getCurrentUid();
        SecretDTO secretDTO = userInnerService.getSecretByUid(userId);
        ExecuteCodeRes response = codeSandbox.executeCode(executeCodeRequest, secretDTO.getSecretId(),
                secretDTO.getSecretKey());
        return response;
    }
}
