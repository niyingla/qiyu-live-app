package org.qiyu.live.gift.provider.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;

import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.idea.qiyu.live.framework.redis.starter.key.GiftProviderCacheKeyBuilder;
import org.qiyu.live.bank.rpc.QiyuCurrencyAccountRpc;
import org.qiyu.live.common.interfaces.topic.GiftProviderTopicNames;
import org.qiyu.live.common.interfaces.utils.ListUtils;
import org.qiyu.live.gift.bo.SendRedPacketBO;
import org.qiyu.live.gift.constans.RedPacketStatusEnum;
import org.qiyu.live.gift.dto.RedPacketConfigReqDTO;
import org.qiyu.live.gift.dto.RedPacketReceiveDTO;
import org.qiyu.live.gift.provider.dao.mapper.RedPacketConfigMapper;
import org.qiyu.live.gift.provider.dao.po.RedPacketConfigPO;
import org.qiyu.live.gift.provider.service.IRedPacketConfigService;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.im.dto.ImMsgBody;
import org.qiyu.live.im.router.constants.ImMsgBizCodeEum;
import org.qiyu.live.im.router.interfaces.ImRouterRpc;
import org.qiyu.live.living.interfaces.dto.LivingRoomReqDTO;
import org.qiyu.live.living.interfaces.rpc.ILivingRoomRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RedPacketConfigServiceImpl implements IRedPacketConfigService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedPacketConfigServiceImpl.class);

    @Resource
    RedPacketConfigMapper redPacketConfigMapper;

    @Resource
    RedisTemplate<String,Object> redisTemplate;

    @Resource
    GiftProviderCacheKeyBuilder cacheKeyBuilder;

    @DubboReference
    ImRouterRpc routerRpc;

    @DubboReference
    ILivingRoomRpc livingRoomRpc;

    @DubboReference
    QiyuCurrencyAccountRpc currencyAccountRpc;

    @Resource
    MQProducer mqProducer;


    @Override
    public RedPacketConfigPO queryByAnchorId(Long anchorId) {
        LambdaQueryWrapper<RedPacketConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RedPacketConfigPO::getAnchorId, anchorId);
        queryWrapper.eq(RedPacketConfigPO::getStatus, RedPacketStatusEnum.WAIT.getCode());
        queryWrapper.orderByDesc(RedPacketConfigPO::getCreateTime);
        queryWrapper.last("limit 1");
        RedPacketConfigPO redPacketConfigPO = redPacketConfigMapper.selectOne(queryWrapper);
        return redPacketConfigPO;
    }

    @Override
    public RedPacketConfigPO queryByConfigCode(String code) {
        LambdaQueryWrapper<RedPacketConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RedPacketConfigPO::getConfigCode, code);
        queryWrapper.eq(RedPacketConfigPO::getStatus, RedPacketStatusEnum.IS_PREPARED.getCode());
        queryWrapper.orderByDesc(RedPacketConfigPO::getCreateTime);
        queryWrapper.last("limit 1");
        return redPacketConfigMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean addOne(RedPacketConfigPO redPacketConfigPO) {
        redPacketConfigPO.setConfigCode(UUID.randomUUID().toString());
        return redPacketConfigMapper.insert(redPacketConfigPO) > 0;
    }

    @Override
    public boolean updateById(RedPacketConfigPO redPacketConfigPO) {
        return redPacketConfigMapper.updateById(redPacketConfigPO) > 0;
    }

    @Override
    public boolean prepareRedPacket(Long anchorId) {
        RedPacketConfigPO configPO = this.queryByAnchorId(anchorId);
        if(configPO==null){
            return false;
        }
        Boolean lockStatus = redisTemplate.opsForValue().setIfAbsent(cacheKeyBuilder.buildRedPacketInitLock(configPO.getConfigCode()), 1, 3L, TimeUnit.SECONDS);
        if (!lockStatus) {
            return false;
        }
        Integer totalCount = configPO.getTotalCount();
        Integer totalPrice = configPO.getTotalPrice();
        String code= configPO.getConfigCode();
        List<Integer> redPacketPriceList = this.createRedPacketPriceList(totalPrice, totalCount);
        String cacheKey = cacheKeyBuilder.buildRedPacketList(code);
        //redis 缓冲区
        List<List<Integer>> lists = ListUtils.splistList(redPacketPriceList, 100);

        for (List<Integer> list:lists){
            redisTemplate.opsForList().leftPushAll(cacheKey, list.toArray());
        }
        redisTemplate.expire(cacheKey,1,TimeUnit.DAYS);
        configPO.setStatus(RedPacketStatusEnum.IS_PREPARED.getCode());
        this.updateById(configPO);
        redisTemplate.opsForValue().set(cacheKeyBuilder.buildRedPacketPrepareSuccess(code),1,1,TimeUnit.DAYS);
        return true;
    }

    @Override
    public RedPacketReceiveDTO receiveRedPacket(RedPacketConfigReqDTO redPacketConfigReqDTO) {
        String code = redPacketConfigReqDTO.getRedPacketConfigCode();
        String cacheKey = cacheKeyBuilder.buildRedPacketList(code);
        Object priceobj = redisTemplate.opsForList().rightPop(cacheKey);
        if(priceobj==null){
            return null;
        }
        Integer price=(Integer) priceobj;
        LOGGER.info("[redPacketConfigReqDTO is]"+redPacketConfigReqDTO);
        /**
         * 异步mq消费
         */
        SendRedPacketBO sendRedPacketBO = new SendRedPacketBO();
        sendRedPacketBO.setPrice(price);
        sendRedPacketBO.setReqDTO(redPacketConfigReqDTO);
        Message message = new Message();
        message.setTopic(GiftProviderTopicNames.RECEIVE_RED_PACKET);
        message.setBody(JSON.toJSONBytes(sendRedPacketBO));
        try {
            SendResult sendResult = mqProducer.send(message);
            if(SendStatus.SEND_OK.equals(sendResult.getSendStatus())){
                LOGGER.info("send sucesss sendResult is{}" ,sendResult);
            }
        } catch (Exception e) {
            LOGGER.error("send fail"+e);
            return new RedPacketReceiveDTO(null, "抱歉，红包被人抢走了，再试试");
        }
        return new RedPacketReceiveDTO(price,"恭喜领取到红包：" + price + "旗鱼币！");
    }
    @Override
    public void receiveRedPacketHandler(RedPacketConfigReqDTO reqDTO, Integer price) {
        String code = reqDTO.getRedPacketConfigCode();
        String totalGetCountCacheKey = cacheKeyBuilder.buildRedPacketTotalGetCount(code);
        String totalGetPriceCacheKey = cacheKeyBuilder.buildRedPacketTotalGetPrice(code);
        // 记录该用户总共领取了多少金额的红包
        redisTemplate.opsForValue().increment(cacheKeyBuilder.buildUserTotalGetPrice(reqDTO.getUserId()), price);
        redisTemplate.opsForHash().increment(totalGetCountCacheKey, code, 1);
        redisTemplate.expire(totalGetCountCacheKey, 1L, TimeUnit.DAYS);
        redisTemplate.opsForHash().increment(totalGetPriceCacheKey, code, price);
        redisTemplate.expire(totalGetPriceCacheKey, 1L, TimeUnit.DAYS);
        // 往用户的余额里增加金额
        LOGGER.info("[reqDTO] is"+reqDTO);
        currencyAccountRpc.incr(reqDTO.getUserId(), price);
        // 持久化红包雨的totalGetCount和totalGetPrice
        redPacketConfigMapper.incrTotalGetPrice(code, price);
        redPacketConfigMapper.incrTotalGetCount(code);
    }

    @Override
    public Boolean startRedPacket(RedPacketConfigReqDTO reqDTO) {
        String code = reqDTO.getRedPacketConfigCode();
        // 红包没有准备好，则返回false
        if (!redisTemplate.hasKey(cacheKeyBuilder.buildRedPacketPrepareSuccess(code))) {
            return false;
        }
        //已经有别的线程发起广播
        String notify = cacheKeyBuilder.buildRedPacketNotify(code);
        if (redisTemplate.hasKey(notify)) {
            return false;
        }
        RedPacketConfigPO configPO = this.queryByConfigCode(code);
        //广播im事件
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("redPacketConfig",JSON.toJSONString(configPO));
        LivingRoomReqDTO roomReqDTO = new LivingRoomReqDTO();
        roomReqDTO.setRoomId(reqDTO.getRoomId());
        roomReqDTO.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
        List<Long> userIdList = livingRoomRpc.queryUserIdsByRoomId(roomReqDTO);
        if(CollectionUtils.isEmpty(userIdList)){
            LOGGER.info("[userIdList] is null");
            return false;
        }
        this.batchSendImMsg(userIdList,ImMsgBizCodeEum.START_RED_PACKET,jsonObject );
        configPO.setStatus(RedPacketStatusEnum.IS_SEND.getCode());
        this.updateById(configPO);
        redisTemplate.opsForValue().set(notify,1,1,TimeUnit.DAYS);
        return true;
    }



    /**
     * 生成红包金额List集合数据
     * @param totalPrice
     * @param totalCount
     * @return
     */
    private  List<Integer> createRedPacketPriceList(Integer totalPrice, Integer totalCount) {
        List<Integer> redPacketPriceList = new ArrayList<>(totalCount);
        for (int i = 0; i < totalCount; i++) {
            if (i + 1 == totalCount) {
                // 如果是最后一个红包
                redPacketPriceList.add(totalPrice);
                break;
            }
            int maxLimit = (totalPrice / (totalCount - i)) * 2;// 最大限额为平均值的两倍
            int currentPrice = ThreadLocalRandom.current().nextInt(1, maxLimit);
            totalPrice -= currentPrice;
            redPacketPriceList.add(currentPrice);
        }
        return redPacketPriceList;
    }
    /**
     * 批量发送im消息
     */
    private void batchSendImMsg(List<Long> userIdList, ImMsgBizCodeEum imMsgBizCodeEum, JSONObject jsonObject) {
        List<ImMsgBody> imMsgBodies = userIdList.stream().map(userId -> {
            ImMsgBody imMsgBody = new ImMsgBody();
            imMsgBody.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
            imMsgBody.setBizCode(imMsgBizCodeEum.getCode());
            imMsgBody.setUserId(userId);
            imMsgBody.setData(jsonObject.toJSONString());
            return imMsgBody;
        }).collect(Collectors.toList());
        routerRpc.batchSendMsg(imMsgBodies);
    }






}
