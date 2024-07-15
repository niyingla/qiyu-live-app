package org.qiyu.live.im.core.server.handler.tcp;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.annotation.Resource;
import org.qiyu.live.im.core.server.common.ChannelHandlerContextCache;
import org.qiyu.live.im.core.server.common.ImContextUtils;
import org.qiyu.live.im.core.server.common.ImMsg;
import org.qiyu.live.im.core.server.handler.ImHandlerFactory;
import org.qiyu.live.im.core.server.interfaces.constans.ImCoreServerConstants;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class TcpImServerCoreHandler extends SimpleChannelInboundHandler {

    @Resource
    ImHandlerFactory imHandlerFactory;
    @Resource
    RedisTemplate<String, Object> redisTemplate;

    /**
     * 处理read 事件
     * @param channelHandlerContext
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        System.out.println("进入TcpImServerCoreHandler");
        if(!(msg instanceof ImMsg)) {
            throw new IllegalArgumentException("error msg, msg is :" + msg);
        }
        ImMsg imMsg = (ImMsg) msg;
        imHandlerFactory.doMsgHandler(channelHandlerContext, imMsg);
    }

    /**
     * 客户端正常或意外掉线，都会触发这里
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Long userId = ImContextUtils.getUserId(ctx);
        Integer appId = ImContextUtils.getAppId(ctx);
        if(userId!=null&&appId!=null){
            ChannelHandlerContextCache.remove(userId);
            redisTemplate.delete(ImCoreServerConstants.IM_BIND_IP_KEY+appId+":"+userId);
        }
    }
}


