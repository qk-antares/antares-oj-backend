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
 * @TableName conversation
 */
@TableName(value ="conversation")
@Data
public class Conversation implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fromUid;
    private Long toUid;
    private Integer fromUnread;
    private Integer toUnread;
    private String lastMessage;
    private Date createTime;
    private Date updateTime;
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}