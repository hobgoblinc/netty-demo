package com.lantian.network.netty.server.handler;

import com.lantian.network.netty.Message;
import com.lantian.network.netty.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoHandler extends SimpleChannelInboundHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EchoHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Message) {
            Message message = (Message) msg;
            if (message.getType() == MessageType.ECHO) {
                String echo = message.getEcho();
                LOGGER.info("收到来自 [{}] 的消息 : {}", ctx.channel().id(), echo);
            } else {
                ctx.fireChannelRead(msg);
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)  {
        LOGGER.error("exception caught");
        LOGGER.error(cause.getMessage(), cause);
    }


}
