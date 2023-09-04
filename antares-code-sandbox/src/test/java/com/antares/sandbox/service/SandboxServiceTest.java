package com.antares.sandbox.service;

import cn.hutool.core.io.resource.ResourceUtil;
import com.antares.sandbox.model.dto.ExecuteCodeRequest;
import com.antares.sandbox.model.dto.ExecuteCodeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@SpringBootTest
public class SandboxServiceTest {
    @Resource
    private SandboxService sandboxService;

    @Test
    void testLocalJudge(){
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("()", "[()]", "(]"));
        String code = ResourceUtil.readStr("testCode/simpleCompute/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");

        ExecuteCodeResponse response = sandboxService.execute(executeCodeRequest);
        System.out.println(response);
    }
}
