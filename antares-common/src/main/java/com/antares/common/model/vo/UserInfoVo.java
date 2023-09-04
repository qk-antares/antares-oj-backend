package com.antares.common.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserInfoVo implements Serializable {
    private Long uid;
    private String username;
    private String userRole;
    private String accessKey;
    private String secretKey;
    private List<UserTagVo> tags;
    private String signature;
    private String email;
    private String phone;
    private Integer sex;
    private String avatar;
    private Integer follow;
    private Integer fans;
    private Integer topic;

    private Boolean isFollow = false;
}