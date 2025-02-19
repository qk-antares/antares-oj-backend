package com.antares.codesandbox.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeReq {
    //一组输入
    private List<String> inputList;
    //提交代码
    private String code;
    //执行语言
    private String language;
}
