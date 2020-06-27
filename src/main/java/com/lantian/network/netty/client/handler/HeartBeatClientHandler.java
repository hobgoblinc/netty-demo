package com.lantian.network.netty.client.handler;

import com.lantian.network.netty.Message;
import com.lantian.network.netty.MessageType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(HeartBeatClientHandler.class);

    public static final ConcurrentMap<String, Channel> CHANNEL_HOLDER = new ConcurrentHashMap<>();

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            log.info("触发了[{}]超时事件[{}]", ctx.channel(), ((IdleStateEvent) evt).state());
            Message message = new Message();
            message.setType(MessageType.HEARTBEAT);
            ctx.channel().writeAndFlush(message);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String id = ctx.channel().id().asLongText();
        log.info("客户[{}]进线成功", id);
        CHANNEL_HOLDER.put(id, ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Message message = (Message) msg;
        if (message.getType() == MessageType.ECHO) {
            log.info("收到服务器消息消息[{}]", message.getEcho());
        }
        ReferenceCountUtil.release(msg);
    }

}
