package com.antares.judge.model.dto.problemsubmit;

import javax.validation.constraints.Min;

import lombok.Data;

/**
 * 创建请求
 */
@Data
public class ProblemSubmitAddReq {

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 题目 id
     */
    @Min(1)
    private Long problemId;
}