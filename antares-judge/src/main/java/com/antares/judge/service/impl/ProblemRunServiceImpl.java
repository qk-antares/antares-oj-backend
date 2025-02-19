package com.antares.judge.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.antares.codesandbox.sdk.model.dto.ExecuteCodeReq;
import com.antares.codesandbox.sdk.model.dto.ExecuteCodeRes;
import com.antares.common.mapper.UserMapper;
import com.antares.common.model.entity.User;
import com.antares.common.utils.TokenUtils;
import com.antares.judge.codesandbox.CodeSandbox;
import com.antares.judge.codesandbox.CodeSandboxFactory;
import com.antares.judge.service.ProblemRunService;
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
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getAccessKey, User::getSecretKey).eq(User::getUid, userId));
        ExecuteCodeRes response = codeSandbox.executeCode(executeCodeRequest, user.getAccessKey(),
                user.getSecretKey());

        return response;
    }
}
