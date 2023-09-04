package com.antares.member.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName user_tag
 */
@TableName(value ="user_tag")
@Data
public class UserTag implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 类别id
     */
    private Long parentId;

    /**
     * 创建用户
     */
    private Long createdBy;

    /**
     * 标签名
     */
    private String name;

    /**
     * 颜色
     */
    private String color;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除标志
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}