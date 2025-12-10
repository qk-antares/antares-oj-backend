package com.antares.codesandbox.sdk.client;

import com.antares.codesandbox.sdk.constant.UrlConstant;
import com.antares.codesandbox.sdk.model.dto.ExecuteCodeReq;
import com.antares.codesandbox.sdk.model.dto.ExecuteCodeRes;
import com.antares.codesandbox.sdk.utils.SignUtils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Antares
 * @date 2023/9/3 8:16
 * @description 调用代码沙箱的客户端
 */
@Slf4j
public class CodeSandboxClient {
    private String accessKey;
    private String secretKey;
    private String gatewayHost;

    public CodeSandboxClient(String accessKey, String secretKey, String gatewayHost) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        if (gatewayHost != null) {
            this.gatewayHost = gatewayHost;
        } else {
            this.gatewayHost = UrlConstant.GATEWAY_HOST;
        }
    }

    public ExecuteCodeRes executeCode(ExecuteCodeReq executeCodeRequest) {
        String requestBodyJson = JSONUtil.toJsonStr(executeCodeRequest);
        try (HttpResponse response = HttpRequest.post(gatewayHost + "/api/sandbox/execute")
                .header("Content-Type", "application/json")
                .header("accessKey", accessKey)
                .header("sign", SignUtils.genSign(requestBodyJson, secretKey))
                .body(requestBodyJson)
                .execute()) {
            String responseBody = response.body();
            log.info("响应：{}", response);
            return JSONUtil.toBean(responseBody, ExecuteCodeRes.class);
        } catch (Exception e) {
            log.info("请求沙箱失败：", e);
            return null;
        }
    }
}
