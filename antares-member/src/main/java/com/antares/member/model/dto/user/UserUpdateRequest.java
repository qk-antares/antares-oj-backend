package com.antares.member.model.dto.user;

import lombok.Data;

import java.util.List;

@Data
public class UserUpdateRequest {
    private Long uid;
    private String username;
    private String password;
    private List<Long> tags;
    private String signature;
    private String email;
    private String phone;
    private Integer sex;
    private String avatar;
}
