package com.antares.codesandbox.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteResult {
    // 退出码
    private Integer exitCode;
    // 正常输出
    private String stdout;
    // 错误输出
    private String stderr;
    // 运行时间
    private Long time;
    // 消耗内存
    private Long memory;
}
