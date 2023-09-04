package com.antares.oj.service.impl;

import com.antares.common.model.vo.UserInfoVo;
import com.antares.oj.codesandbox.CodeSandbox;
import com.antares.oj.codesandbox.CodeSandboxFactory;
import com.antares.oj.model.dto.problemrun.ProblemRunRequest;
import com.antares.oj.model.enums.ExecuteCodeStatusEnum;
import com.antares.oj.model.vo.problemrun.ProblemRunResult;
import com.antares.oj.service.ProblemRunService;
import com.antares.sandbox.sdk.model.dto.executecode.ExecuteCodeRequest;
import com.antares.sandbox.sdk.model.dto.executecode.ExecuteCodeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
public class ProblemRunServiceImpl implements ProblemRunService {
    @Value("${codesandbox.type:remote}")
    private String type;
    @Resource
    private CodeSandboxFactory codeSandboxFactory;

    @Override
    public ProblemRunResult doProblemRun(ProblemRunRequest problemRunRequest, UserInfoVo currentUser) {
        String code = problemRunRequest.getCode();
        String language = problemRunRequest.getLanguage();
        List<String> inputList = Collections.singletonList(problemRunRequest.getInput());

        CodeSandbox codeSandbox = codeSandboxFactory.newInstance(type);
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse response = codeSandbox.executeCode(executeCodeRequest, currentUser.getAccessKey(), currentUser.getSecretKey());

        return getProblemRunVo(problemRunRequest.getInput(), response);
    }

    private static ProblemRunResult getProblemRunVo(String input, ExecuteCodeResponse response) {
        ProblemRunResult problemRunResult = new ProblemRunResult();
        problemRunResult.setInput(input);
        //执行成功
        if(response.getCode().equals(ExecuteCodeStatusEnum.SUCCESS.getValue())){
            problemRunResult.setCode(ExecuteCodeStatusEnum.SUCCESS.getValue());
            problemRunResult.setOutput(response.getResults().get(0).getOutput());
        } else if(response.getCode().equals(ExecuteCodeStatusEnum.RUN_FAILED.getValue())){
            problemRunResult.setCode(ExecuteCodeStatusEnum.RUN_FAILED.getValue());
            problemRunResult.setOutput(response.getMsg());
        } else if(response.getCode().equals(ExecuteCodeStatusEnum.COMPILE_FAILED.getValue())){
            problemRunResult.setCode(ExecuteCodeStatusEnum.COMPILE_FAILED.getValue());
            problemRunResult.setOutput(response.getMsg());
        }
        return problemRunResult;
    }
}
