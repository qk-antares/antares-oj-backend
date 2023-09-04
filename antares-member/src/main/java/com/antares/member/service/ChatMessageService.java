package com.antares.member.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.antares.member.model.dto.chat.MessageQueryRequest;
import com.antares.member.model.entity.ChatMessage;
import com.antares.member.model.vo.chat.MessageVo;

import javax.servlet.http.HttpServletRequest;

/**
* @author Antares
* @description 针对表【message】的数据库操作Service
* @createDate 2023-05-18 21:30:23
*/
public interface ChatMessageService extends IService<ChatMessage> {

    Page<MessageVo> listMessageVoByPage(MessageQueryRequest messageQueryRequest, HttpServletRequest request);

    MessageVo messageToMessageVo(ChatMessage chatMessage);

    Long saveMessage(ChatMessage chatMessage);
}
