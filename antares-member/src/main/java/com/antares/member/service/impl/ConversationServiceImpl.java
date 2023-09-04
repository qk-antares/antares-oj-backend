package com.antares.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import com.antares.common.constant.RedisConstants;
import com.antares.common.model.dto.UsernameAndAvtarDto;
import com.antares.common.model.vo.UserInfoVo;
import com.antares.common.utils.BeanCopyUtils;
import com.antares.common.utils.PageRequest;
import com.antares.member.mapper.ConversationMapper;
import com.antares.member.model.entity.Conversation;
import com.antares.member.model.vo.chat.ConversationVo;
import com.antares.member.service.ConversationService;
import com.antares.member.service.UserService;
import com.antares.member.utils.RedisUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Antares
* @description 针对表【conversation】的数据库操作Service实现
* @createDate 2023-05-18 21:30:08
*/
@Service
@Slf4j
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation>
    implements ConversationService{
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Page<ConversationVo> listConversationVoByPage(PageRequest pageRequest, HttpServletRequest request) {
        UserInfoVo currentUser = redisUtils.getCurrentUserWithValidation(request);
        Long uid = currentUser.getUid();
        int pageNum = pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize();

        //数据库中的数据除了访问量其他数据都可以确保是最新的
        //1. 构造查询条件
        QueryWrapper<Conversation> queryWrapper = new QueryWrapper<Conversation>()
                .eq("to_uid", uid).or().eq("from_uid", uid)
                .orderBy(true, false, "update_time");

        //2. 查询数据库中的信息
        long start = System.currentTimeMillis();
        Page<Conversation> conversationPage = page(new Page<>(pageNum, pageSize), queryWrapper);
        long end = System.currentTimeMillis();

        //3. 转换为vos
        List<ConversationVo> vos = conversationsToVos(conversationPage.getRecords(), uid);
        Page<ConversationVo> conversationVoPage = new Page<>(pageNum, pageSize, conversationPage.getTotal());
        conversationVoPage.setRecords(vos);
        return conversationVoPage;
    }

    @Override
    public ConversationVo getConversationByTargetUid(Long targetUid, HttpServletRequest request) {
        UserInfoVo currentUser = redisUtils.getCurrentUserWithValidation(request);
        Long uid = currentUser.getUid();
        //todo: 异步编排优化，同步查询conversation和dto
        Conversation conversation = lambdaQuery().eq(Conversation::getFromUid, targetUid)
                .eq(Conversation::getToUid, uid)
                .or()
                .eq(Conversation::getFromUid, uid)
                .eq(Conversation::getToUid, targetUid).one();
        UsernameAndAvtarDto dto = userService.getUsernameAndAvatar(targetUid);
        if(conversation == null){
            return new ConversationVo(-1L, targetUid, dto.getUsername(), dto.getAvatar(), 0, "", new Date());
        }
        ConversationVo vo = BeanCopyUtils.copyBean(conversation, ConversationVo.class);
        vo.setFromUid(targetUid);
        vo.setFromUsername(dto.getUsername());
        vo.setAvatar(dto.getAvatar());
        vo.setUnread(uid.equals(conversation.getFromUid()) ? conversation.getFromUnread() : conversation.getToUnread());
        return vo;
    }

    @Override
    @Transactional
    public void clearUnread(Long uid, Long conversationId) {
        Conversation conversation = getById(conversationId);
        if(conversation.getFromUid().equals(uid) && conversation.getFromUnread() > 0){
            update(new UpdateWrapper<Conversation>().setSql("from_unread = 0")
                    .eq("id", conversationId));
            //更新redis缓存
            String cacheKey = RedisConstants.NOTIFICATION_PREFIX + uid + RedisConstants.MSG_NOTIFICATION_SUFFIX;
            stringRedisTemplate.opsForValue().decrement(cacheKey, conversation.getFromUnread());
        } else if (conversation.getToUid().equals(uid) && conversation.getToUid() > 0){
            update(new UpdateWrapper<Conversation>().setSql("to_unread = 0")
                    .eq("id", conversationId));
            String cacheKey = RedisConstants.NOTIFICATION_PREFIX + uid + RedisConstants.MSG_NOTIFICATION_SUFFIX;
            stringRedisTemplate.opsForValue().decrement(cacheKey, conversation.getToUnread());
        }
    }

    @Override
    @Transactional
    public void clearConversationUnread(Long uid) {
        update(new UpdateWrapper<Conversation>().setSql("from_unread = 0")
                .eq("from_uid", uid));
        update(new UpdateWrapper<Conversation>().setSql("to_unread = 0")
                .eq("to_uid", uid));
    }


    private List<ConversationVo> conversationsToVos(List<Conversation> conversations, Long uid) {
        //涉及到的所有用户
        List<Long> fromUids = conversations.stream()
                .map(conversation -> conversation.getFromUid().equals(uid) ? conversation.getToUid() : conversation.getFromUid())
                .collect(Collectors.toList());
        Map<Long, UsernameAndAvtarDto> map = userService.listUserNameAndAvatarByUids(fromUids).stream()
                .collect(Collectors.toMap(UsernameAndAvtarDto::getUid, dto -> dto));

        List<ConversationVo> vos = conversations.stream().map(conversation -> {
            ConversationVo vo = BeanCopyUtils.copyBean(conversation, ConversationVo.class);
            UsernameAndAvtarDto dto = map.get(conversation.getFromUid().equals(uid) ? conversation.getToUid() : conversation.getFromUid());
            vo.setFromUid(dto.getUid());
            vo.setFromUsername(dto.getUsername());
            vo.setAvatar(dto.getAvatar());
            vo.setUnread(conversation.getFromUid().equals(uid) ?
                    conversation.getFromUnread() : conversation.getToUnread());
            return vo;
        }).collect(Collectors.toList());
        return vos;
    }
}




