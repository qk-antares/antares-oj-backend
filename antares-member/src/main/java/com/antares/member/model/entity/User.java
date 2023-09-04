package com.antares.member.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
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
     * 密码
     */
    private String password;

    /**
     * 用户角色
     */
    private String userRole;

    private String accessKey;
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
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 关注
     */
    private Integer follow;

    /**
     * 粉丝
     */
    private Integer fans;

    /**
     * 博客/动态
     */
    private Integer topic;

    /**
     * 第三方id
     */
    private String socialUid;

    /**
     * 访问第三方信息的token
     */
    private String accessToken;

    /**
     * 访问令牌的有效期
     */
    private long expiresIn;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}