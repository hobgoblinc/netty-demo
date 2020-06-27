package com.lantian.network.netty.server.handler;

import com.lantian.network.netty.Message;
import com.lantian.network.netty.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(HeartBeatHandler.class);

    private static ConcurrentMap<String, Long> aliveChannels = new ConcurrentHashMap<>();


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Message message = (Message) msg;
        if (message.getType() == MessageType.HEARTBEAT) {
            log.info("服务端收到来自[{}]的心跳", ctx.channel().id());
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * 定时检测连接活跃性
     * 如果15分钟时间都没有心跳，则断连
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        log.info("触发了[{}]超时事件[{}]", ctx.channel().id().asLongText(), ((IdleStateEvent) evt).state());
        if (evt instanceof IdleStateEvent) {
            String id = ctx.channel().id().asLongText();
            Long lastActive = aliveChannels.get(id);
            if (lastActive == null || lastActive + 15 * 60 * 1000 < System.currentTimeMillis()) {
                aliveChannels.remove(id);
                ctx.disconnect();
            } else {
                Message message = new Message();
                message.setType(MessageType.ECHO);
                message.setEcho("are you ok?");
                ctx.writeAndFlush(message);
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String id = ctx.channel().id().asLongText();
        if (aliveChannels.get(id) == null) {
            log.info("客户[{}]进线", id);
            aliveChannels.put(id, System.currentTimeMillis());
        }
        super.channelActive(ctx);
    }
}
