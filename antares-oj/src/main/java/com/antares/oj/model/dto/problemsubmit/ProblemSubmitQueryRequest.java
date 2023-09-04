package com.antares.oj.model.dto.problemsubmit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.antares.common.utils.PageRequest;

import java.io.Serializable;

/**
 * 查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProblemSubmitQueryRequest extends PageRequest implements Serializable {
    /**
     * 题目 id
     */
    private Long problemId;

    private static final long serialVersionUID = 1L;
}