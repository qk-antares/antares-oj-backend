package com.antares.common.model.vo;

import lombok.Data;

@Data
public class UserTagVo {
    private Long id;
    private Long parentId;
    private String name;
    private String color;
}
