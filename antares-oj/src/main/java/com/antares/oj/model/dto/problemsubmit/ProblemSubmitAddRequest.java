package com.antares.oj.model.dto.problemsubmit;

import lombok.Data;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 创建请求
 */
@Data
public class ProblemSubmitAddRequest implements Serializable {

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

    private static final long serialVersionUID = 1L;
}