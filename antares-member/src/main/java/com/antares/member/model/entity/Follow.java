package com.antares.member.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName follow
 */
@TableName(value ="follow")
@Data
public class Follow implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long uid;
    private Long followUid;
    private Integer unread;
    private Date createTime;
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}