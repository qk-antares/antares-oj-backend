package com.antares.sandbox.sdk.model.dto.executecode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {
    //一组输入
    private List<String> inputList;
    //提交代码
    private String code;
    //执行语言
    private String language;
}
