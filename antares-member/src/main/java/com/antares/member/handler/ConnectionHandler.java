package com.antares.member.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import com.antares.member.job.NettyStarter;
import com.antares.member.model.dto.chat.Command;
import com.antares.member.model.dto.chat.Response;
import com.antares.member.model.enums.WebSocketCodeEnum;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConnectionHandler {
    public void execute(ChannelHandlerContext ctx, Command command){
        Long uid = command.getUid();
        //连接已经建立了（重复登录）
        if(NettyStarter.USERS.containsKey(uid)){
            ctx.channel().writeAndFlush(Response.error(WebSocketCodeEnum.CONNECTED));
            ctx.channel().disconnect();
            return;
        }
        MutablePair<Channel, Long> conversationChannelPair = new MutablePair<>(ctx.channel(), 0L);
        NettyStarter.USERS.put(uid, conversationChannelPair);
        ctx.channel().writeAndFlush(Response.ok());

        //添加关闭连接事件监听
        ctx.channel().closeFuture().addListener((ChannelFutureListener) future -> {
            NettyStarter.USERS.remove(uid);
            log.info("{}的连接已经断开", uid);
        });
    }
}
