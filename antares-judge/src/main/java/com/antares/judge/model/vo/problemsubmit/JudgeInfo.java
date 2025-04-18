package com.antares.judge.model.vo.problemsubmit;

import lombok.Data;

/**
 * 判题信息（由ExecuteCodeRes转）
 */
@Data
public class JudgeInfo {
    // 状态码
    private Integer status;
    // 程序执行信息
    private String msg;

    // 通过用例数
    private Integer pass;
    // 总用例数
    private Integer total;
    // 消耗内存
    private Long memory;
    // 消耗时间（KB）
    private Long time;

    // 未通过的最后一个用例信息
    private String input;
    private String output;
    private String expectedOutput;
}
