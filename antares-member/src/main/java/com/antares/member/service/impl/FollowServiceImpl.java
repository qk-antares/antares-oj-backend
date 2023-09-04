package com.antares.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.antares.common.model.vo.UserInfoVo;
import com.antares.member.mapper.FollowMapper;
import com.antares.member.mapper.UserMapper;
import com.antares.member.model.entity.Follow;
import com.antares.member.model.entity.User;
import com.antares.member.model.vo.user.FollowVo;
import com.antares.member.service.FollowService;
import com.antares.member.service.UserService;
import com.antares.member.utils.RedisUtils;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
* @author Antares
* @description 针对表【follow】的数据库操作Service实现
* @createDate 2023-05-15 15:15:38
*/
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow>
    implements FollowService{
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;

    @Override
    public void follow(Long uid, HttpServletRequest request) {
        UserInfoVo currentUser = redisUtils.getCurrentUserWithValidation(request);

        Follow follow = new Follow();
        follow.setUid(currentUser.getUid());
        follow.setFollowUid(uid);

        // 发送关注消息到队列，最后一个参数是消息的唯一ID
        rabbitTemplate.convertAndSend("exchange.direct", "follow", follow,
                new CorrelationData(UUID.randomUUID().toString()));
    }

    @Override
    public List<FollowVo> getFollowsOfCurrent(HttpServletRequest request) {
        UserInfoVo currentUser = redisUtils.getCurrentUserWithValidation(request);
        List<FollowVo> vos = lambdaQuery().select(Follow::getFollowUid, Follow::getUnread)
                .eq(Follow::getUid, currentUser.getUid())
                .orderBy(true, false, Follow::getUpdateTime)
                .list().stream().map(follow -> {
                    FollowVo vo = new FollowVo();
                    vo.setUid(follow.getFollowUid());
                    vo.setUnread(follow.getUnread());
                    return vo;
                })
                .collect(Collectors.toList());

        List<Long> uids = vos.stream().map(FollowVo::getUid).collect(Collectors.toList());
        if(uids.isEmpty()){
            return new ArrayList<>();
        }
        Map<Long, User> userMap = userMapper.selectBatchIds(uids).stream()
                .collect(Collectors.toMap(User::getUid, user -> user));
        for (FollowVo vo : vos) {
            vo.setUsername(userMap.get(vo.getUid()).getUsername());
            vo.setAvatar(userMap.get(vo.getUid()).getAvatar());
        }
        return vos;
    }

    @Override
    public List<UserInfoVo> getFollowsByUid(Long uid, HttpServletRequest request) {
        List<Long> followIds = lambdaQuery().select(Follow::getFollowUid).eq(Follow::getUid, uid)
                .list().stream().map(Follow::getFollowUid).collect(Collectors.toList());
        List<UserInfoVo> vos = userService.getUserListByUids(followIds, request);
        return vos;
    }

    @Override
    public List<UserInfoVo> getFansByUid(Long uid, HttpServletRequest request) {
        List<Long> fanIds = lambdaQuery().select(Follow::getUid).eq(Follow::getFollowUid, uid)
                .list().stream().map(Follow::getUid).collect(Collectors.toList());
        List<UserInfoVo> vos = userService.getUserListByUids(fanIds, request);
        return vos;
    }

    @Override
    public List<Long> getFollowIdsOfCurrent(HttpServletRequest request) {
        UserInfoVo currentUser = redisUtils.getCurrentUserWithValidation(request);
        List<Long> followIds = lambdaQuery().select(Follow::getFollowUid).eq(Follow::getUid, currentUser.getUid())
                .list().stream().map(Follow::getFollowUid).collect(Collectors.toList());
        return followIds;
    }
}




