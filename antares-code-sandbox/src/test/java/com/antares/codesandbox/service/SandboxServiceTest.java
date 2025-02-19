package com.antares.codesandbox.service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.antares.codesandbox.model.dto.ExecuteCodeReq;
import com.antares.codesandbox.model.dto.ExecuteCodeRes;

import cn.hutool.core.io.resource.ResourceUtil;

@SpringBootTest
public class SandboxServiceTest {
    @Resource
    private SandboxService sandboxService;

    @Test
    void testLocalJudge(){
        ExecuteCodeReq executeCodeRequest = new ExecuteCodeReq();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4", "5 6"));
        String code = ResourceUtil.readStr("testCode/simpleCompute/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");

        ExecuteCodeRes response = sandboxService.execute(executeCodeRequest);
        System.out.println(response);
    }
}
