package org.qiyu.live.im.core.server.service.impl;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.idea.qiyu.live.framework.redis.starter.key.ImCoreServerProviderCacheKeyBuilder;
import org.qiyu.live.common.interfaces.topic.ImCoreServerProviderTopicNames;
import org.qiyu.live.im.core.server.service.IMsgAckCheckService;
import org.qiyu.live.im.dto.ImMsgBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class IMsgAckCheckServiceImpl implements IMsgAckCheckService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IMsgAckCheckServiceImpl.class);

    @Resource
    private MQProducer mqProducer;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ImCoreServerProviderCacheKeyBuilder cacheKeyBuilder;

    @Override
    public void doMsgAck(ImMsgBody imMsgBody) {
        LOGGER.info("进入doMsgAck");
        String key = cacheKeyBuilder.buildImAckMapKey(imMsgBody.getUserId(), imMsgBody.getAppId());
        LOGGER.info("进入doMsgAck的key为："+key);
        System.out.println("delete="+redisTemplate.opsForHash().delete(key, imMsgBody.getMsgId()));;
        redisTemplate.expire(key,30, TimeUnit.MINUTES);
    }

    @Override
    public void recordMsgAck(ImMsgBody imMsgBody, int times) {

        String key = cacheKeyBuilder.buildImAckMapKey(imMsgBody.getUserId(), imMsgBody.getAppId());

        redisTemplate.opsForHash().put(key, imMsgBody.getMsgId(), times);
        System.out.println("记录完成------"+key+ imMsgBody.getMsgId());
        redisTemplate.expire(key,30, TimeUnit.MINUTES);
    }

    @Override
    public void sendDelayMsg(ImMsgBody imMsgBody) {

        String json = JSON.toJSONString(imMsgBody);
        Message message = new Message();
        message.setBody(json.getBytes());
        message.setTopic(ImCoreServerProviderTopicNames.QIYU_LIVE_IM_ACK_MSG_TOPIC);
        //等级1 -> 1s，等级2 -> 5s
        message.setDelayTimeLevel(1);
        try {
            SendResult sendResult = mqProducer.send(message);
            LOGGER.info("[MsgAckCheckServiceImpl] msg is {},sendResult is {}", json, sendResult);
        } catch (Exception e) {
            LOGGER.error("[MsgAckCheckServiceImpl] error is ", e);
        }
    }



    @Override
    public int getMsgAckTimes(String msgId, Long userId, int appId) {
        String key = cacheKeyBuilder.buildImAckMapKey(userId, appId);
        Object value = redisTemplate.opsForHash().get(key, msgId);

        if (value == null) {
            return -1;
        }
        return (int) value;
    }
}
