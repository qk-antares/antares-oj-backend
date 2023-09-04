package com.antares.member.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import com.antares.member.model.dto.chat.Command;
import com.antares.member.model.dto.chat.Response;
import com.antares.member.model.enums.CommandType;
import com.antares.member.model.enums.WebSocketCodeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ChannelHandler.Sharable
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Resource
    private ConnectionHandler connectionHandler;
    @Resource
    private ChatHandler chatHandler;
    @Resource
    private ChangeHandler changeHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        try {
            Command command = JSON.parseObject(frame.text(), Command.class);
            switch (CommandType.match(command.getCode())){
                case CONNECTION:
                    connectionHandler.execute(ctx, command);break;
                case CHAT:
                    chatHandler.execute(ctx, command);break;
                case CHANGE:
                    changeHandler.execute(ctx, command);break;
                default:
                    ctx.channel().writeAndFlush(Response.error(WebSocketCodeEnum.NOT_EXIST));
            }
        } catch (Exception e) {
            ctx.channel().writeAndFlush(Response.error(WebSocketCodeEnum.INTERNAL_SERVER_ERROR));
        }
    }
}
