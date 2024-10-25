package com.antares.judge.service;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.antares.codesandbox.sdk.model.dto.executecode.ExecuteCodeRequest;
import com.antares.codesandbox.sdk.model.dto.executecode.ExecuteCodeResponse;
import com.antares.codesandbox.sdk.model.enums.ExecuteCodeStatusEnum;
import com.antares.common.mapper.UserMapper;
import com.antares.common.model.dto.problemrun.ProblemRunRequest;
import com.antares.common.model.entity.User;
import com.antares.common.model.vo.problemrun.ProblemRunResult;
import com.antares.common.service.judge.ProblemRunService;
import com.antares.common.utils.TokenUtils;
import com.antares.judge.codesandbox.CodeSandbox;
import com.antares.judge.codesandbox.CodeSandboxFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@Service
public class ProblemRunServiceImpl implements ProblemRunService {
    @Resource
    private CodeSandboxFactory codeSandboxFactory;
    @Resource
    private UserMapper userMapper;
    @Value("${antares.code-sandbox.type:remote}")
    private String type;

    @Override
    public ProblemRunResult doProblemRun(ProblemRunRequest problemRunRequest, String token) {
        String code = problemRunRequest.getCode();
        String language = problemRunRequest.getLanguage();
        List<String> inputList = Collections.singletonList(problemRunRequest.getInput());

        CodeSandbox codeSandbox = codeSandboxFactory.newInstance(type);
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();

        Long userId = TokenUtils.getUidFromToken(token);
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getAccessKey, User::getSecretKey).eq(User::getUid, userId));
        ExecuteCodeResponse response = codeSandbox.executeCode(executeCodeRequest, user.getAccessKey(),
                user.getSecretKey());

        return getProblemRunVo(problemRunRequest.getInput(), response);
    }

    private static ProblemRunResult getProblemRunVo(String input, ExecuteCodeResponse response) {
        ProblemRunResult problemRunResult = new ProblemRunResult();
        problemRunResult.setInput(input);
        // 执行成功
        if (response.getCode().equals(ExecuteCodeStatusEnum.SUCCESS.getValue())) {
            problemRunResult.setCode(ExecuteCodeStatusEnum.SUCCESS.getValue());
            problemRunResult.setOutput(response.getResults().get(0).getOutput());
        } else if (response.getCode().equals(ExecuteCodeStatusEnum.RUN_FAILED.getValue())) {
            problemRunResult.setCode(ExecuteCodeStatusEnum.RUN_FAILED.getValue());
            problemRunResult.setOutput(response.getMsg());
        } else if (response.getCode().equals(ExecuteCodeStatusEnum.COMPILE_FAILED.getValue())) {
            problemRunResult.setCode(ExecuteCodeStatusEnum.COMPILE_FAILED.getValue());
            problemRunResult.setOutput(response.getMsg());
        }
        return problemRunResult;
    }
}
