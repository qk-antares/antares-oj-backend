package com.antares.common.mybatis.dto;

import com.antares.common.mybatis.constant.SqlConstant;

import lombok.Data;

/**
 * 分页请求
 */
@Data
public class PageReq {

    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int size = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = SqlConstant.SORT_ORDER_DESC;
}
