package org.qiyu.live.im.core.server.handler.impl;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.qiyu.live.im.constants.ImMsgCodeEnum;
import org.qiyu.live.im.core.server.common.ChannelHandlerContextCache;
import org.qiyu.live.im.core.server.common.ImContextUtils;
import org.qiyu.live.im.core.server.common.ImMsg;
import org.qiyu.live.im.core.server.handler.SimplyHandler;
import org.qiyu.live.im.core.server.interfaces.constans.ImCoreServerConstants;
import org.qiyu.live.im.dto.ImMsgBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 登出消息的处理逻辑统一收拢到这个类中
 *
 * @Author idea
 * @Date: Created in 20:40 2023/7/6
 * @Description
 */
@Component
public class LogoutMsgHandler implements SimplyHandler {
    @Resource
    StringRedisTemplate stringRedisTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutMsgHandler.class);
    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg imMsg) {
        Long userId = ImContextUtils.getUserId(ctx);
        Integer appId = ImContextUtils.getAppId(ctx);
        if (userId == null || appId == null) {
            LOGGER.error("body error, imMsgBody is {}", new String(imMsg.getBody()));
            //有可能是错误的消息包导致，直接放弃连接
            ctx.close();
            throw new IllegalArgumentException("attr is error");
        }
        ImMsgBody respBody = new ImMsgBody();
        respBody.setAppId(appId);
        respBody.setUserId(userId);
        respBody.setData("true");
        ImMsg respMsg = ImMsg.build(ImMsgCodeEnum.IM_LOGOUT_MSG.getCode(), JSON.toJSONString(respBody));
        ctx.writeAndFlush(respMsg);
        LOGGER.info("[LogoutMsgHandler] logout success,userId is {},appId is {}", userId, appId);
        ChannelHandlerContextCache.remove(userId);
        stringRedisTemplate.delete(ImCoreServerConstants.IM_BIND_IP_KEY + appId + ":" + userId);
//        handlerLogout(userId, appId);
        ImContextUtils.removeUserId(ctx);
        ImContextUtils.removeAppId(ctx);
        ctx.close();
    }
//    public void handlerLogout(Long userId, Integer appId) {
//        // 理想情况下：客户端短线的时候发送短线消息包
//        ChannelHandlerContextCache.remove(userId);
//        // 删除供Router取出的存在Redis的IM服务器的ip+端口地址
//        stringRedisTemplate.delete(ImCoreServerConstants.IM_BIND_IP_KEY + appId + ":" + userId);
//        // 删除心跳包存活缓存
//        stringRedisTemplate.delete(cacheKeyBuilder.buildImLoginTokenKey(userId, appId));
//    }
}