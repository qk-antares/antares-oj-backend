package com.antares.oj.model.dto.problem;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.antares.common.utils.PageRequest;

import java.io.Serializable;
import java.util.List;

/**
 * 查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ProblemQueryRequest extends PageRequest implements Serializable {
    /**
     * 标签列表
     */
    private List<String> tags;
    private String status;
    private String difficulty;
    private String keyword;

    private static final long serialVersionUID = 1L;
}