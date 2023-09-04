package com.antares.sandbox.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {
    /**
     * 执行信息
     */
    private String msg;
    /**
     * 执行状态
     */
    private Integer code;
    /**
     * 执行结果
     */
    private List<ExecuteResult> results;

}
