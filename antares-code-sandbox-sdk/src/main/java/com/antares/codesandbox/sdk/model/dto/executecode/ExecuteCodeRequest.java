package com.antares.codesandbox.sdk.model.dto.executecode;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
