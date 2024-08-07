package org.qiyu.live.gift.provider.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
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
import org.qiyu.live.framework.mq.starter.properties.RocketMQConsumerProperties;
import org.qiyu.live.gift.constans.SendGiftTypeEnum;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.im.dto.ImMsgBody;
import org.qiyu.live.im.router.constants.ImMsgBizCodeEum;
import org.qiyu.live.im.router.interfaces.ImRouterRpc;
import org.qiyu.live.living.interfaces.dto.LivingRoomReqDTO;
import org.qiyu.live.living.interfaces.dto.LivingRoomRespDTO;
import org.qiyu.live.living.interfaces.rpc.ILivingRoomRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Component
public class SendGiftConsumer implements InitializingBean {

    private static final Long PK_INIT_NUM = 50L;
    private static final Long PK_MAX_NUM = 1000L;
    private static final Long PK_MIN_NUM = 0L;

    private String LUA_SCRIPT =
            "if (redis.call('exists', KEYS[1])) == 1 then " +
                    " local currentNum=redis.call('get',KEYS[1]) " +
                    " if (tonumber(currentNum)<=tonumber(ARGV[2]) and tonumber(currentNum)>=tonumber(ARGV[3])) then " +
                    " return redis.call('incrby',KEYS[1],tonumber(ARGV[4])) " +
                    " else return currentNum end " +
                    "else " +
                    "redis.call('set', KEYS[1], tonumber(ARGV[1])) " +
                    "redis.call('EXPIRE', KEYS[1], 3600 * 12) " +
                    "return ARGV[1] end";

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

    @DubboReference
    private ILivingRoomRpc livingRoomRpc;





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
                JSONObject jsonObject = new JSONObject();
                Integer sendGiftMqType = sendGiftMq.getType();

                if(tradeRespDTO.isSuccess()){
                   Long receiverId= sendGiftMq.getReceiverId();
                    if(SendGiftTypeEnum.DEFAULT_SEND_GIFT.getCode().equals(sendGiftMqType)){
                        jsonObject.put("url", sendGiftMq.getUrl());
                        LivingRoomReqDTO reqDTO = new LivingRoomReqDTO();
                        reqDTO.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
                        reqDTO.setRoomId(sendGiftMq.getRoomId());
                        List<Long> userIdList = livingRoomRpc.queryUserIdsByRoomId(reqDTO);
                        this.batchSendImMsg(userIdList, ImMsgBizCodeEum.LIVING_ROOM_SEND_GIFT_SUCCESS, jsonObject);
                    }else if (SendGiftTypeEnum.PK_SEND_GIFT.getCode().equals(sendGiftMqType)){
                        this.pkImMsgSend(jsonObject,sendGiftMq,receiverId);
//                        //pk类型的送礼，要通知给什么养的用户
//                        //url礼物特效全直播间可见
//                        //todo 进度条全直播间可见
//                        Integer roomId = sendGiftMq.getRoomId();
//                        // 2 TODO PK进度条全直播间可见
//                        String pkNumKey = cacheKeyBuilder.buildLivingPkKey(roomId);
//                        String incrKey = cacheKeyBuilder.buildLivingPkSendSeq(roomId);
//
//                        LivingRoomRespDTO respDTO = livingRoomRpc.queryByRoomId(roomId);//TODO 虚拟数据，获取方式还未完善
//                        Long pkObjId = livingRoomRpc.queryOnlinePkUserId(roomId);
//                        if(pkObjId==null|| respDTO==null||respDTO.getAnchorId()==null){
//                            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//                        }
//                        Long pkUserId = respDTO.getAnchorId();
//                        Long resultNum;
//                        Long pkNum = 0L;
//                        // 获取该条消息的序列号，避免消息乱序
//                        Long sendGiftSeqNum = redisTemplate.opsForValue().increment(incrKey);
//                        if (receiverId.equals(pkUserId)) {
//                            // 收礼人是房主userId，则进度条增加
//                            resultNum = redisTemplate.opsForValue().increment(pkNumKey, sendGiftMq.getPrice());
//                            if (PK_MAX_NUM <= resultNum) {
//                                jsonObject.put("winnerId", pkUserId);
//                                // 返回给前端的pkNum最大为MAX_NUM
//                                pkNum = PK_MAX_NUM;
//                            } else {
//                                pkNum = resultNum;
//                            }
//                        } else if (receiverId.equals(pkObjId)) {
//                            // 收礼人是来挑战的，则进图条减少
//                            resultNum = redisTemplate.opsForValue().decrement(pkNumKey, sendGiftMq.getPrice());
//                            if (PK_MIN_NUM >= resultNum) {
//                                jsonObject.put("winnerId", pkObjId);
//                                // 返回给前端的pkNum最小为MIN_NUM
//                                pkNum = PK_MIN_NUM;
//                            } else {
//                                pkNum = resultNum;
//                            }
//                        }
//                        jsonObject.put("sendGiftSeqNum", sendGiftSeqNum);
//                        jsonObject.put("pkNum", pkNum);
//                        LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
//                        livingRoomReqDTO.setRoomId(roomId);
//                        livingRoomReqDTO.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
//
//                        List<Long> userIdsByRoomId = livingRoomRpc.queryUserIdsByRoomId(livingRoomReqDTO);
//                        jsonObject.put("url",sendGiftMq.getUrl());
//                        batchSendImMsg(userIdsByRoomId,ImMsgBizCodeEum.LIVING_ROOM_PK_SEND_GIFT_SUCCESS,jsonObject);
                    }

                }else{
                    jsonObject.put("msg",tradeRespDTO.getMsg());
                    sendImMsgSingleton(sendGiftMq.getUserId(),ImMsgBizCodeEum.LIVING_ROOM_SEND_GIFT_FAIL.getCode(),jsonObject );
                }
                LOGGER.info("send msg");
                LOGGER.info("[SendGiftConsumer] msg is {}", msg);

            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        mqPushConsumer.start();
        LOGGER.info("mq消费者启动成功,namesrv is {}", rocketMQConsumerProperties.getNameSrv());
    }

    private void pkImMsgSend(JSONObject jsonObject, SendGiftMq sendGiftMq, Long receiverId) {
        //pk类型的送礼 要通知什么给直播间的用户
        //url 礼物特效全直播间可见
        //todo 进度条全直播间可见
        // 1000,进度条长度一共是1000，每个礼物对于进度条的影响就是一个数值（500（A）：500（B），550：450）
        // 直播pk进度是不是以roomId为维度，string，送礼（A）incr，送礼给（B）就是decr。
        Integer roomId = sendGiftMq.getRoomId();
        String isOverCacheKey = cacheKeyBuilder.buildLivingPkIsOver(roomId);
        if (redisTemplate.hasKey(isOverCacheKey)) {
            return;
        }
        LivingRoomRespDTO respDTO = livingRoomRpc.queryByRoomId(roomId);
        Long pkObjId = livingRoomRpc.queryOnlinePkUserId(roomId);
        if (pkObjId == null || respDTO == null || respDTO.getAnchorId() == null) {
            return;
        }
        Long pkUserId = respDTO.getAnchorId();
        Long pkNum = 0L;
        String pkNumKey = cacheKeyBuilder.buildLivingPkKey(roomId);
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript();
        redisScript.setScriptText(LUA_SCRIPT);
        redisScript.setResultType(Long.class);
        Long sendGiftSeqNum = System.currentTimeMillis();
        if (pkUserId.equals(receiverId)) {
            Integer moveStep = sendGiftMq.getPrice() / 10;
            pkNum = this.redisTemplate.execute(redisScript, Collections.singletonList(pkNumKey), PK_INIT_NUM, PK_MAX_NUM, PK_MIN_NUM, moveStep);
            if (PK_MAX_NUM <= pkNum) {
                jsonObject.put("winnerId", pkUserId);
            }
        } else if (pkObjId.equals(receiverId)) {
            Integer moveStep = sendGiftMq.getPrice() / 10 * -1;
            pkNum = this.redisTemplate.execute(redisScript, Collections.singletonList(pkNumKey), PK_INIT_NUM, PK_MAX_NUM, PK_MIN_NUM, moveStep);
            if (PK_MIN_NUM >= pkNum) {
                this.redisTemplate.opsForValue().set(cacheKeyBuilder.buildLivingPkIsOver(roomId),-1);
                jsonObject.put("winnerId", pkObjId);
            }
        }
        jsonObject.put("receiverId", sendGiftMq.getReceiverId());
        jsonObject.put("sendGiftSeqNum", sendGiftSeqNum);
        jsonObject.put("pkNum", pkNum);
        jsonObject.put("url", sendGiftMq.getUrl());
        LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
        livingRoomReqDTO.setRoomId(roomId);
        livingRoomReqDTO.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
        List<Long> userIdList = livingRoomRpc.queryUserIdsByRoomId(livingRoomReqDTO);
        this.batchSendImMsg(userIdList, ImMsgBizCodeEum.LIVING_ROOM_PK_SEND_GIFT_SUCCESS, jsonObject);
    }

    /**
     * 单独发送礼物
     * @param userId
     * @param bizCode
     * @param jsonObject
     */
    private void sendImMsgSingleton(Long userId, int bizCode, JSONObject jsonObject) {
        ImMsgBody imMsgBody = new ImMsgBody();
        imMsgBody.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
        imMsgBody.setBizCode(bizCode);
        imMsgBody.setUserId(userId);
        imMsgBody.setData(jsonObject.toJSONString());
        routerRpc.sendMsg(imMsgBody);
    }

    /**
     * 批量发送im消息
     * @param userIdList
     * @param imMsgBizCodeEnum
     * @param jsonObject
     */
    private void batchSendImMsg(List<Long> userIdList, ImMsgBizCodeEum imMsgBizCodeEnum, JSONObject jsonObject) {
        List<ImMsgBody> imMsgBodies = userIdList.stream().map(userId -> {
            ImMsgBody imMsgBody = new ImMsgBody();
            imMsgBody.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
            imMsgBody.setBizCode(imMsgBizCodeEnum.getCode());
            imMsgBody.setUserId(userId);
            imMsgBody.setData(jsonObject.toJSONString());
            return imMsgBody;
        }).collect(Collectors.toList());
        routerRpc.batchSendMsg(imMsgBodies);
    }
}
