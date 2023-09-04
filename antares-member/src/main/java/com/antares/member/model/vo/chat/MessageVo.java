package com.antares.member.model.vo.chat;

import lombok.Data;

import java.util.Date;

@Data
public class MessageVo {
    private Long id;
    private Long conversationId;
    private Long fromUid;
    private String fromUsername;
    private String avatar;
    private String content;
    private Date createTime;
}
