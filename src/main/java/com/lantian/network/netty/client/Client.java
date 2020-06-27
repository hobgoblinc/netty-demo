package com.lantian.network.netty.client;

import com.lantian.network.netty.Message;
import com.lantian.network.netty.MessageType;
import com.lantian.network.netty.client.handler.HeartBeatClientHandler;
import com.lantian.network.netty.codec.ProtostuffDecoder;
import com.lantian.network.netty.codec.ProtostuffEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Client {

    private static final Logger log = LoggerFactory.getLogger(Client.class);

    public void connect(String host, int port) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("ping", new IdleStateHandler(16, 0,
                                    0, TimeUnit.SECONDS));
                            p.addLast("decoder", new ProtostuffDecoder());
                            p.addLast("encoder", new ProtostuffEncoder());
                            p.addLast(new HeartBeatClientHandler());
                        }
                    });
            ChannelFuture future = b.connect(host, port).sync();
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }
        // 定时发送echo的任务
        Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()).scheduleAtFixedRate(() -> {
            for (Map.Entry<String, Channel> entry : HeartBeatClientHandler.CHANNEL_HOLDER.entrySet()) {
                Channel server = entry.getValue();
                Message message = new Message();
                message.setType(MessageType.ECHO);
                message.setEcho("i am ok~");
                log.info("write echo");
                server.writeAndFlush(message);
            }
        }, 0,20000, TimeUnit.MILLISECONDS);
        new Client().connect("127.0.0.1", port);
    }


}
