package com.antares.sandbox.service;

import cn.hutool.core.io.resource.ResourceUtil;
import com.antares.sandbox.model.dto.ExecuteCodeResponse;
import com.antares.sandbox.template.java.JavaNativeAcmSandbox;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class TestNativeAcm {
    public static void main(String[] args) {
        JavaNativeAcmSandbox javaNativeAcmSandbox = new JavaNativeAcmSandbox();

        String code = ResourceUtil.readStr("testCode/simpleCompute/Main.java", StandardCharsets.UTF_8);
        ExecuteCodeResponse executeCodeResponse = javaNativeAcmSandbox.executeJavaCode(Collections.singletonList("1 3"), code);
        System.out.println(executeCodeResponse);
    }
}
