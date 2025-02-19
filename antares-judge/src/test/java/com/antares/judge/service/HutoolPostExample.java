package com.antares.judge.service;

import java.util.HashMap;
import java.util.Map;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

public class HutoolPostExample {
    public static void main(String[] args) {
        // 设置URL并附加查询参数
        String url = "https://api.json.cn/try_run?action=get_token";
        
        String code = """
                int a = 10;
                System.out.println(a);
                """;

        System.out.println(code);

        // 构建表单数据
        Map<String, Object> formData = new HashMap<>();
        formData.put("source_code", "int a = 10;\nSystem.out.println(a);");  // 多行代码
        formData.put("language_id", 62);
        formData.put("command_line_arguments", "");
        formData.put("stdin", "1 2");

        // 发送POST请求
        HttpResponse response = HttpRequest.post(url)
            .form(formData)  // 设置表单数据
            .execute();      // 执行请求

        // 输出响应内容
        System.out.println(response.body());
    }
}
