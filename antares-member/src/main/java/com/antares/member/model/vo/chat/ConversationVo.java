package com.antares.member.model.vo.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationVo {
    private Long id;
    private Long fromUid;
    private String fromUsername;
    private String avatar;

    private Integer unread;
    private String lastMessage;
    private Date updateTime;
}
