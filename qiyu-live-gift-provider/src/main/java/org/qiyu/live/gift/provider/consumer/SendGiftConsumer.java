package org.qiyu.live.gift.provider.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.idea.qiyu.live.framework.redis.starter.key.GiftProviderCacheKeyBuilder;
import org.qiyu.live.bank.dto.AccountTradeReqDTO;
import org.qiyu.live.bank.dto.AccountTradeRespDTO;
import org.qiyu.live.bank.rpc.QiyuCurrencyAccountRpc;
import org.qiyu.live.common.interfaces.dto.SendGiftMq;
import org.qiyu.live.common.interfaces.topic.GiftProviderTopicNames;
import org.qiyu.live.common.interfaces.topic.ImCoreServerProviderTopicNames;
import org.qiyu.live.framework.mq.starter.properties.RocketMQConsumerProperties;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.im.dto.ImMsgBody;
import org.qiyu.live.im.router.constants.ImMsgBizCodeEum;
import org.qiyu.live.im.router.interfaces.ImRouterRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
public class SendGiftConsumer implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendGiftConsumer.class);
    @Resource
    private RocketMQConsumerProperties rocketMQConsumerProperties;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    
    @Resource
    private GiftProviderCacheKeyBuilder cacheKeyBuilder;
    
    @DubboReference
    QiyuCurrencyAccountRpc qiyuCurrencyAccountRpc;

    @DubboReference
    ImRouterRpc routerRpc;

    @Override
    public void afterPropertiesSet() throws Exception {
        DefaultMQPushConsumer mqPushConsumer = new DefaultMQPushConsumer();
        //老版本中会开启，新版本的mq不需要使用到
        mqPushConsumer.setVipChannelEnabled(false);
        mqPushConsumer.setNamesrvAddr(rocketMQConsumerProperties.getNameSrv());
        mqPushConsumer.setConsumerGroup(rocketMQConsumerProperties.getGroupName() + "_" + SendGiftConsumer.class.getSimpleName());
        //一次从broker中拉取10条消息到本地内存当中进行消费
        mqPushConsumer.setConsumeMessageBatchMaxSize(10);
        mqPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        //监听im发送过来的业务消息topic
        mqPushConsumer.subscribe(GiftProviderTopicNames.SEND_GIFT,"");
        mqPushConsumer.setMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                SendGiftMq sendGiftMq = JSON.parseObject(new String(msg.getBody()), SendGiftMq.class);
                String cacheKey = cacheKeyBuilder.buildGiftConsumeKey(sendGiftMq.getUuid());
                Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(cacheKey, -1, 5, TimeUnit.MINUTES);
                if(!ifAbsent){
                    //曾经消费过
                    continue;
                }
                AccountTradeReqDTO accountTradeReqDTO = new AccountTradeReqDTO();
                accountTradeReqDTO.setUserId(sendGiftMq.getUserId());
                accountTradeReqDTO.setNum(sendGiftMq.getPrice());
                AccountTradeRespDTO tradeRespDTO = qiyuCurrencyAccountRpc.consumeForSendGift(accountTradeReqDTO);

                //余额扣减成功
                ImMsgBody imMsgBody = new ImMsgBody();
                imMsgBody.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
                JSONObject jsonObject = new JSONObject();
                if(tradeRespDTO.isSuccess()){
                    imMsgBody.setBizCode(ImMsgBizCodeEum.LIVING_ROOM_SEND_GIFT_SUCCESS.getCode());
                    imMsgBody.setUserId(sendGiftMq.getReceiverId());
                    jsonObject.put("url",sendGiftMq.getUrl());
                    imMsgBody.setData(jsonObject.toJSONString());
                }else{
                    imMsgBody.setUserId(sendGiftMq.getUserId());
                    imMsgBody.setBizCode(ImMsgBizCodeEum.LIVING_ROOM_SEND_GIFT_FAIL.getCode());
                    jsonObject.put("msg",tradeRespDTO.getMsg());
                    imMsgBody.setData(jsonObject.toJSONString());

                }
                LOGGER.info("send msg");
                routerRpc.sendMsg(imMsgBody);
                LOGGER.info("[SendGiftConsumer] msg is {}", msg);

            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        mqPushConsumer.start();
        LOGGER.info("mq消费者启动成功,namesrv is {}", rocketMQConsumerProperties.getNameSrv());
    }
}