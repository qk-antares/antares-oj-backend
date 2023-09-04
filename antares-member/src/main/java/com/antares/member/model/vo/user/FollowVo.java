package com.antares.member.model.vo.user;

import lombok.Data;

@Data
public class FollowVo {
    private Long uid;
    private String username;
    private String avatar;
    private Integer unread;
}
