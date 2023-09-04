package com.antares.sandbox.sdk.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.antares.sandbox.sdk.model.dto.executecode.ExecuteCodeRequest;
import com.antares.sandbox.sdk.model.dto.executecode.ExecuteCodeResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.antares.sandbox.sdk.utils.SignUtils.genSign;

/**
 * @author Antares
 * @date 2023/9/3 8:16
 * @description 调用代码沙箱的客户端
 */
@AllArgsConstructor
@Slf4j
public class CodeSandboxClient {
    public static final String GATEWAY_HOST = "http://oj.antares.cool";
    private String accessKey;
    private String secretKey;

    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String requestBodyJson = JSONUtil.toJsonStr(executeCodeRequest);
        try (HttpResponse response = HttpRequest.post(GATEWAY_HOST + "/api/sandbox/execute")
                .header("Content-Type", "application/json")
                .header("accessKey", accessKey)
                .header("sign", genSign(requestBodyJson, secretKey))
                .body(requestBodyJson)
                .execute()) {
            String responseBody = response.body();
            log.info("响应：{}", response);
            return JSONUtil.toBean(responseBody, ExecuteCodeResponse.class);
        } catch (Exception e) {
            log.info("请求沙箱失败：", e);
            return null;
        }
    }
}
