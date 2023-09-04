package com.antares.sandbox.sdk;

import com.antares.sandbox.sdk.client.CodeSandboxClient;
import com.antares.sandbox.sdk.model.dto.executecode.ExecuteCodeRequest;
import com.antares.sandbox.sdk.model.dto.executecode.ExecuteCodeResponse;

import java.util.Arrays;

public class TestClient {
    public static void main(String[] args) {
        String accessKey = "dd7431b1a7a83629c6fbef1924a99ea01d7114ca";
        String secretKey = "7f4f3e19bff33bd80dcb302e6a9ab48f18600f56";

        CodeSandboxClient codeSandboxClient = new CodeSandboxClient(accessKey, secretKey);
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 3", "2 4"));
        String code = "import java.io.*;\nimport java.util.*;\n\npublic class Main {\npublic static void main(String args[]) throws Exception {\nScanner cin = new Scanner(System.in);\nint a=cin.nextInt(), b=cin.nextInt();\nSystem.out.println(a+b);\n}\n}";
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");

        ExecuteCodeResponse response = codeSandboxClient.executeCode(executeCodeRequest);

        System.out.println(response);
    }
}
