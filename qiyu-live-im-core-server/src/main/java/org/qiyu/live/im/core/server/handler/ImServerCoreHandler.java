package org.qiyu.live.im.core.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.qiyu.live.im.core.server.common.ImMsg;
import org.qiyu.live.im.core.server.handler.impl.ImHandlerFactoryImpl;

public class ImServerCoreHandler extends SimpleChannelInboundHandler {
    private ImHandlerFactory imHandlerFactory = new ImHandlerFactoryImpl();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {


        if (!(msg instanceof ImMsg)) {
            throw new IllegalArgumentException("error msg type,msg is " + msg);
        }
        ImMsg imMsg = (ImMsg) msg;
        imHandlerFactory.doMsgHandler(ctx, imMsg);
    }
}


