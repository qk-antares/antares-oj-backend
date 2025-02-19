package com.antares.common.model.dto.problem;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * 创建请求
 */
@Data
@NotNull
public class ProblemAddReq implements Serializable {

    /**
     * 标题
     */
    @NotBlank
    private String title;

    /**
     * 内容
     */
    @NotBlank
    private String content;

    /**
     * 难度
     */
    @NotBlank
    private String difficulty;

    /**
     * 标签列表
     */
    @NotNull
    private List<String> tags;

    /**
     * 题目答案
     */
    private String answer;

    /**
     * 判题用例
     */
    @NotNull
    private List<JudgeCase> judgeCase;

    /**
     * 判题配置
     */
    @NotNull
    private JudgeConfig judgeConfig;

    private static final long serialVersionUID = 1L;
}