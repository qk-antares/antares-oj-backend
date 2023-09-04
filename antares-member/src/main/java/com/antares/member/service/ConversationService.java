package com.antares.member.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.antares.common.utils.PageRequest;
import com.antares.member.model.entity.Conversation;
import com.antares.member.model.vo.chat.ConversationVo;

import javax.servlet.http.HttpServletRequest;

/**
* @author Antares
* @description 针对表【conversation】的数据库操作Service
* @createDate 2023-05-18 21:30:08
*/
public interface ConversationService extends IService<Conversation> {

    Page<ConversationVo> listConversationVoByPage(PageRequest pageRequest, HttpServletRequest request);

    ConversationVo getConversationByTargetUid(Long targetUid, HttpServletRequest request);

    void clearUnread(Long uid, Long conversationId);

    void clearConversationUnread(Long uid);
}