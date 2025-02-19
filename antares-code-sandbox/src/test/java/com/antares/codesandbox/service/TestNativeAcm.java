package com.antares.codesandbox.service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import com.antares.codesandbox.model.dto.ExecuteCodeRes;
import com.antares.codesandbox.template.java.JavaNativeAcmSandbox;

import cn.hutool.core.io.resource.ResourceUtil;

public class TestNativeAcm {
    public static void main(String[] args) {
        JavaNativeAcmSandbox javaNativeAcmSandbox = new JavaNativeAcmSandbox();

        String code = ResourceUtil.readStr("testCode/simpleCompute/Main.java", StandardCharsets.UTF_8);
        ExecuteCodeRes executeCodeResponse = javaNativeAcmSandbox.executeCode(
                Collections.singletonList("1 3"), code, ".java");
        System.out.println(executeCodeResponse);
    }
}
