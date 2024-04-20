package org.qiyu.live.im.core.server.handler.impl;

import io.netty.channel.ChannelHandlerContext;
import org.qiyu.live.im.core.server.common.ImMsg;
import org.qiyu.live.im.core.server.handler.SimplyHandler;

/**
 * 登出消息的处理逻辑统一收拢到这个类中
 *
 * @Author idea
 * @Date: Created in 20:40 2023/7/6
 * @Description
 */
public class LogoutMsgHandler implements SimplyHandler {

    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg imMsg) {
        System.out.println("[logout]:" + imMsg);
        ctx.writeAndFlush(imMsg);
    }
}