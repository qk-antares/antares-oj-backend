package com.antares.codesandbox.sdk.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRes {
    /**
     * 执行状态码
     */
    private Integer code;

    /**
     * 执行信息
     */
    private String msg;

    /**
     * 执行结果
     */
    private List<ExecuteResult> results;
}
