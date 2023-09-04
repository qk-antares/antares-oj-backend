package com.antares.member.job;

import com.antares.member.handler.WebSocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class NettyStarter implements CommandLineRunner {
    @Resource
    private WebSocketHandler webSocketHandler;

    public static final Map<Long, MutablePair<Channel, Long>> USERS = new ConcurrentHashMap<>(1024);

    @Override
    public void run(String... args) throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new HttpServerCodec())
                                .addLast(new ChunkedWriteHandler())
                                .addLast(new HttpObjectAggregator(1024 * 64))
                                .addLast(new WebSocketServerProtocolHandler("/"))
                                .addLast(webSocketHandler);
                    }
                });
        bootstrap.bind(8666).sync();
    }
}
