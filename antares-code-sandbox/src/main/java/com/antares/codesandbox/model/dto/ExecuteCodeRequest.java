package com.antares.codesandbox.model.dto;

import java.util.List;

import lombok.Data;

@Data
public class ExecuteCodeRequest {
    //一组输入
    private List<String> inputList;
    //提交代码
    private String code;
    //执行语言
    private String language;
}
