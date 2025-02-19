package com.antares.common.model.dto.problemsubmit;

import java.io.Serializable;

import com.antares.common.model.dto.PageReq;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProblemSubmitQueryRequest extends PageReq implements Serializable {
    /**
     * 题目 id
     */
    private Long problemId;

    private static final long serialVersionUID = 1L;
}