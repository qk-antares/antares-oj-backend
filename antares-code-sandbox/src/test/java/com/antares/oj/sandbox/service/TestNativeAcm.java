package com.antares.oj.sandbox.service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import com.antares.codesandbox.model.dto.ExecuteCodeResponse;
import com.antares.codesandbox.template.java.JavaNativeAcmSandbox;

import cn.hutool.core.io.resource.ResourceUtil;

public class TestNativeAcm {
    public static void main(String[] args) {
        JavaNativeAcmSandbox javaNativeAcmSandbox = new JavaNativeAcmSandbox();

        String code = ResourceUtil.readStr("testCode/simpleCompute/Main.java", StandardCharsets.UTF_8);
        ExecuteCodeResponse executeCodeResponse = javaNativeAcmSandbox.executeJavaCode(Collections.singletonList("1 3"), code);
        System.out.println(executeCodeResponse);
    }
}
