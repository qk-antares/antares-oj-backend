package com.antares.member.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.rabbitmq.client.Channel;
import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.AppHttpCodeEnum;
import com.antares.member.mapper.UserMapper;
import com.antares.member.model.entity.Follow;
import com.antares.member.model.entity.User;
import com.antares.member.service.FollowService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;

@RabbitListener(queues = {"antares.member.follow"})
@Service
public class FollowListener {
    @Resource
    private FollowService followService;
    @Resource
    private UserMapper userMapper;

    /**
     * 监听消息队列处理消息
     * 多个服务争抢式处理消息队列中的消息
     * 处理完一个消息才能处理下一个
     * 队列中多种类型的消息：@RabbitListener（类上） + @RabbitHandler（方法上），实际应用可以创建专门处理消息的实体类
     * @param message
     * @param follow
     * @param channel
     */
    @RabbitHandler
    @Transactional
    public void handleFollowMessage(Message message, Follow follow, Channel channel) {
        // 执行关注逻辑
        LambdaQueryChainWrapper<Follow> wrapper = followService.lambdaQuery()
                .eq(Follow::getUid, follow.getUid())
                .eq(Follow::getFollowUid, follow.getFollowUid());
        Follow one = wrapper.one();
        //已经关注了
        if(one != null) {
            //删除
            followService.removeById(one.getId());
            //修改关注者和被关注者的fans和follow
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .setSql("fans = fans - 1")
                    .eq(User::getUid, follow.getFollowUid()));
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .setSql("follow = follow - 1")
                    .eq(User::getUid, follow.getUid()));
        } else {
            followService.save(follow);
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .setSql("fans = fans + 1")
                    .eq(User::getUid, follow.getFollowUid()));
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .setSql("follow = follow + 1")
                    .eq(User::getUid, follow.getUid()));
        }

        //手动ack（false代表不批量接收）
        try {
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            throw new BusinessException(AppHttpCodeEnum.INTERNAL_SERVER_ERROR, "RabbitMQ连接接异常！");
        }
    }
}
