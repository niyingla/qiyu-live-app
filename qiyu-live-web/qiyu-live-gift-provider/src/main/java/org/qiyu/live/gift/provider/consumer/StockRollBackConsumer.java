package org.qiyu.live.gift.provider.consumer;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.qiyu.live.common.interfaces.topic.GiftProviderTopicNames;
import org.qiyu.live.framework.mq.starter.properties.RocketMQConsumerProperties;
import org.qiyu.live.gift.bo.SendRedPacketBO;
import org.qiyu.live.gift.dto.RollBackStockDTO;
import org.qiyu.live.gift.provider.service.ISkuStockInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class StockRollBackConsumer implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(StockRollBackConsumer.class);
    @Resource
    private RocketMQConsumerProperties rocketMQConsumerProperties;
    @Resource
    private ISkuStockInfoService skuStockInfoService;

    @Override
    public void afterPropertiesSet() throws Exception {
        DefaultMQPushConsumer mqPushConsumer = new DefaultMQPushConsumer();
        //老版本中会开启，新版本的mq不需要使用到
        mqPushConsumer.setVipChannelEnabled(false);
        mqPushConsumer.setNamesrvAddr(rocketMQConsumerProperties.getNameSrv());
        mqPushConsumer.setConsumerGroup(rocketMQConsumerProperties.getGroupName() + "_" + StockRollBackConsumer.class.getSimpleName());
        //一次从broker中拉取10条消息到本地内存当中进行消费
        mqPushConsumer.setConsumeMessageBatchMaxSize(1);
        mqPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        //监听im发送过来的业务消息topic
        mqPushConsumer.subscribe(GiftProviderTopicNames.ROLL_BACK_STOCK, "");
        mqPushConsumer.setMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            try {
                RollBackStockDTO rollBackStockDTO = JSON.parseObject(msgs.get(0).getBody(), RollBackStockDTO.class);
                skuStockInfoService.stockRollBackHandler(rollBackStockDTO);
            }catch (Exception e){
                return  ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        mqPushConsumer.start();
        LOGGER.info("StockRollBackConsumer has started {}", rocketMQConsumerProperties.getNameSrv());
    }
}
