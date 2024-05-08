package org.qiyu.live.im.core.server.handler.impl;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.qiyu.live.common.interfaces.topic.ImCoreServerProviderTopicNames;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.im.constants.ImConstants;
import org.qiyu.live.im.constants.ImMsgCodeEnum;
import org.qiyu.live.im.core.server.common.ChannelHandlerContextCache;
import org.qiyu.live.im.core.server.common.ImContextUtils;
import org.qiyu.live.im.core.server.common.ImMsg;
import org.qiyu.live.im.core.server.handler.SimplyHandler;
import org.qiyu.live.im.core.server.interfaces.constans.ImCoreServerConstants;
import org.qiyu.live.im.core.server.interfaces.dto.ImOnlineDTO;
import org.qiyu.live.im.dto.ImMsgBody;
import org.qiyu.live.im.interfaces.ImTokenRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Component
public class LoginMsgHandler implements SimplyHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginMsgHandler.class);
    @DubboReference
    private ImTokenRpc imTokenRpc;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    MQProducer mqProducer;

    /**
     * 想要建立连接的话，我们需要进行一系列的参数校验，
     * 然后参数无误后，验证存储的userId和消息中的userId是否相同，相同才允许建立连接
     */
    @Override
    public void handler(ChannelHandlerContext ctx, ImMsg imMsg) {
        // 防止重复请求：login允许连接才放如userId，若已经允许连接就不再接收login请求包
        if (ImContextUtils.getUserId(ctx) != null) {
            return;
        }
        byte[] body = imMsg.getBody();
        if (body == null || body.length == 0) {
            ctx.close();
            LOGGER.error("body error, imMsgBody is {}", new String(imMsg.getBody()));
            throw new IllegalArgumentException("body error");
        }
        String s = new String(body);
        ImMsgBody imMsgBody = JSON.parseObject(s, ImMsgBody.class);
        String token = imMsgBody.getToken();
        Long userIdFromMsg = imMsgBody.getUserId();
        Integer appId = imMsgBody.getAppId();
        if (StringUtils.isEmpty(token) || userIdFromMsg < 10000 || appId < 10000) {
            ctx.close();
            LOGGER.error("body error, imMsgBody is {}", new String(imMsg.getBody()));
            throw new IllegalArgumentException("param error");
        }
        Long userId = imTokenRpc.getUserIdByToken(token);
        // 从RPC获取的userId和传递过来的userId相等，则没出现差错，允许建立连接
        if (userId != null && userId.equals(userIdFromMsg)) {
            // 按照userId保存好相关的channel信息
           loginSuccessHandler(ctx,userId,appId,null);
           return;
        }
        // 不允许建立连接
        ctx.close();
        LOGGER.error("token error, imMsg is {}", imMsg);
        throw new IllegalArgumentException("token error");
    }

    public void loginSuccessHandler(ChannelHandlerContext ctx, Long userId, Integer appId, Integer roomId) {
        LOGGER.info("准备登录------");
        // 按照userId保存好相关的channel信息
        ChannelHandlerContextCache.put(userId, ctx);
        // 将userId保存到netty域信息中，用于正常/非正常logout的处理
        ImContextUtils.setUserId(ctx, userId);
        ImContextUtils.setAppId(ctx, appId);
        if (roomId != null) {
            ImContextUtils.setRoomId(ctx, roomId);
        }
        // 将im消息回写给客户端
        ImMsgBody respBody = new ImMsgBody();
        respBody.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
        respBody.setUserId(userId);
        respBody.setData("true");
        ImMsg respMsg = ImMsg.build(ImMsgCodeEnum.IM_LOGIN_MSG.getCode(), JSON.toJSONString(respBody));
        // 将im服务器的ip+端口地址保存到Redis，以供Router服务取出进行转发
        stringRedisTemplate.opsForValue().set(ImCoreServerConstants.IM_BIND_IP_KEY + appId + ":" + userId,
                ChannelHandlerContextCache.getServerIpAddress() +"%"+userId,
                2 * ImConstants.DEFAULT_HEART_BEAT_GAP, TimeUnit.SECONDS);
        LOGGER.info("[LoginMsgHandler] login success, userId is {}, appId is {}", userId, appId);
        sendLoginMQ(userId,appId,roomId);
        ctx.writeAndFlush(respMsg);
    }
    private void sendLoginMQ(Long userId, Integer appId, Integer roomId) {
        ImOnlineDTO imOnlineDTO = new ImOnlineDTO();
        imOnlineDTO.setUserId(userId);
        imOnlineDTO.setAppId(appId);
        imOnlineDTO.setRoomId(roomId);
        imOnlineDTO.setLoginTime(System.currentTimeMillis());
        Message message = new Message();
        System.out.println("imOnlineDTO="+imOnlineDTO);
        message.setTopic(ImCoreServerProviderTopicNames.IM_ONLINE_TOPIC);
        message.setBody(JSON.toJSONString(imOnlineDTO).getBytes());
        try {
            SendResult sendResult = mqProducer.send(message);
            LOGGER.info("[sendLoginMQ] sendResult is {}", sendResult);
        } catch (Exception e) {
            LOGGER.error("[sendLoginMQ] error is: ", e);
        }
    }
}