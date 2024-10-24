package com.antares.codesandbox.model.dto;

import lombok.Data;

@Data
public class ExecuteResult {
    // 退出码
    private Integer exitValue;
    // 正常输出
    private String output;
    // 错误输出
    private String errorOutput;
    // 运行时间
    private Long time;
    // 消耗内存
    private Long memory;
}
