package com.antares.common.model.vo.problemrun;

import lombok.Data;

@Data
public class ProblemRunResult {
    /**
     * 执行状态
     */
    private Integer code;
    /**
     * 输入
     */
    private String input;
    /**
     * 执行结果
     */
    private String output;

}
