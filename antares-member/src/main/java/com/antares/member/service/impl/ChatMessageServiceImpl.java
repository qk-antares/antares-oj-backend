package com.antares.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import com.antares.common.constant.RedisConstants;
import com.antares.common.model.dto.UsernameAndAvtarDto;
import com.antares.common.model.vo.UserInfoVo;
import com.antares.common.utils.BeanCopyUtils;
import com.antares.member.job.NettyStarter;
import com.antares.member.mapper.ChatMessageMapper;
import com.antares.member.mapper.ConversationMapper;
import com.antares.member.model.dto.chat.MessageQueryRequest;
import com.antares.member.model.entity.ChatMessage;
import com.antares.member.model.entity.Conversation;
import com.antares.member.model.vo.chat.MessageVo;
import com.antares.member.service.ChatMessageService;
import com.antares.member.service.UserService;
import com.antares.member.utils.RedisUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Antares
* @description 针对表【message】的数据库操作Service实现
* @createDate 2023-05-18 21:30:23
*/
@Service
@Slf4j
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService {
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private UserService userService;
    @Resource
    private ConversationMapper conversationMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Page<MessageVo> listMessageVoByPage(MessageQueryRequest messageQueryRequest, HttpServletRequest request) {
        UserInfoVo currentUser = redisUtils.getCurrentUserWithValidation(request);
        int pageNum = messageQueryRequest.getPageNum();
        int pageSize = messageQueryRequest.getPageSize();
        Long conversationId = messageQueryRequest.getConversationId();

        //数据库中的数据除了访问量其他数据都可以确保是最新的
        //1. 构造查询条件
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<ChatMessage>()
                .eq("conversation_id", conversationId)
                .orderByDesc("create_time");

        //2. 查询数据库中的信息
        Page<ChatMessage> messagePage = page(new Page<>(pageNum, pageSize), queryWrapper);

        //3. 转换为vos
        List<MessageVo> vos = new ArrayList<>();
        List<ChatMessage> records = messagePage.getRecords();
        if(!records.isEmpty()){
            Collections.reverse(records);
            vos = messagesToVos(records, currentUser);
        }
        Page<MessageVo> messageVoPage = new Page<>(pageNum, pageSize, messagePage.getTotal());
        messageVoPage.setRecords(vos);
        return messageVoPage;
    }

    @Override
    public MessageVo messageToMessageVo(ChatMessage chatMessage) {
        MessageVo vo = BeanCopyUtils.copyBean(chatMessage, MessageVo.class);
        UsernameAndAvtarDto dto = userService.getUsernameAndAvatar(chatMessage.getFromUid());
        vo.setFromUsername(dto.getUsername());
        vo.setAvatar(dto.getAvatar());
        return vo;
    }

    @Override
    @Transactional
    public Long saveMessage(ChatMessage chatMessage) {
        Long toUid = chatMessage.getToUid();
        Long fromUid = chatMessage.getFromUid();
        Long conversationId = chatMessage.getConversationId();

        //两人没有建立conversation
        if(conversationId.equals(-1L)){
            //建立两人之间的conversation
            Conversation insert = new Conversation();
            insert.setFromUid(fromUid);
            insert.setToUid(toUid);
            insert.setLastMessage(chatMessage.getContent());
            insert.setToUnread(1);
            conversationMapper.insert(insert);

            chatMessage.setConversationId(insert.getId());

            // 存储消息
            save(chatMessage);

            //消息的接收者不在线或者没有打开对话
            MutablePair<Channel, Long> pair = NettyStarter.USERS.get(toUid);
            boolean tmpFlag = (pair == null || !conversationId.equals(pair.getRight()));
            //更新redis缓存
            if(tmpFlag){
                String cacheKey = RedisConstants.NOTIFICATION_PREFIX + chatMessage.getToUid() + RedisConstants.MSG_NOTIFICATION_SUFFIX;
                stringRedisTemplate.opsForValue().increment(cacheKey);
            }

            return insert.getId();
        } else {
            Conversation conversation = conversationMapper.selectById(conversationId);
            MutablePair<Channel, Long> pair = NettyStarter.USERS.get(toUid);
            //消息的接收者不在线或者没有打开对话
            boolean tmpFlag = (pair == null || !conversationId.equals(pair.getRight()));
            //是对方发来的消息，我方不在线或没有打开对应的对话
            boolean fromUnreadFlag = fromUid.equals(conversation.getToUid()) && tmpFlag;
            //我方发的消息，对方不在线或没有打开对应的对话
            boolean toUnreadFlag = fromUid.equals(conversation.getFromUid()) && tmpFlag;

            //更新update_time,unread,laseMessage（unread的更新取决于对面在不在线，以及是否打开了对应的对话）
            conversationMapper.update(null, new LambdaUpdateWrapper<Conversation>()
                    .eq(Conversation::getFromUid, fromUid)
                    .eq(Conversation::getToUid, toUid)
                    .or()
                    .eq(Conversation::getFromUid, toUid)
                    .eq(Conversation::getToUid, fromUid)
                    .set(Conversation::getLastMessage, chatMessage.getContent())
                    .set(Conversation::getUpdateTime, new Date())
                    .setSql(fromUnreadFlag, "from_unread = from_unread + 1")
                    .setSql(toUnreadFlag, "to_unread = to_unread + 1"));
            //更新redis缓存
            if(tmpFlag){
                String cacheKey = RedisConstants.NOTIFICATION_PREFIX + chatMessage.getToUid() + RedisConstants.MSG_NOTIFICATION_SUFFIX;
                stringRedisTemplate.opsForValue().increment(cacheKey);
            }

            // 存储消息
            save(chatMessage);
            return conversationId;
        }
    }

    private List<MessageVo> messagesToVos(List<ChatMessage> records, UserInfoVo currentUser) {
        //获取对方的基本信息
        ChatMessage firstMsg = records.get(0);
        Long targetUid = currentUser.getUid().equals(firstMsg.getToUid()) ?
                firstMsg.getFromUid() : firstMsg.getToUid();
        UsernameAndAvtarDto dto = userService.getUsernameAndAvatar(targetUid);

        List<MessageVo> vos = records.stream().map(message -> {
            MessageVo vo = BeanCopyUtils.copyBean(message, MessageVo.class);
            if (message.getFromUid().equals(targetUid)) {
                vo.setFromUsername(dto.getUsername());
                vo.setAvatar(dto.getAvatar());
            } else {
                vo.setFromUid(currentUser.getUid());
                vo.setFromUsername(currentUser.getUsername());
                vo.setAvatar(currentUser.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());
        return vos;
    }
}




