package com.antares.user.model.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User {
    /**
     * 用户id
     */
    @TableId(type = IdType.AUTO)
    private Long uid;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户角色
     */
    private String userRole;

    private String secretId;
    private String secretKey;

    /**
     * 用户标签
     */
    private String tags;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;
}