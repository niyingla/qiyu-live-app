package org.qiyu.live.bank.provider.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.qiyu.live.bank.constants.OrderStatusEnum;
import org.qiyu.live.bank.constants.PayProductTypeEnum;
import org.qiyu.live.bank.dto.PayOrderDTO;
import org.qiyu.live.bank.dto.PayProductDTO;
import org.qiyu.live.bank.provider.dao.mapper.PayOrderMapper;
import org.qiyu.live.bank.provider.dao.po.PayOrderPO;
import org.qiyu.live.bank.provider.dao.po.PayTopicPO;
import org.qiyu.live.bank.provider.service.IPayOrderService;
import org.qiyu.live.bank.provider.service.IPayProductService;
import org.qiyu.live.bank.provider.service.IPayTopicService;
import org.qiyu.live.bank.provider.service.QiyuCurrencyAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class PayOrderServiceImpl implements IPayOrderService {
    @Resource
    private PayOrderMapper payOrderMapper;
    @Resource
    IPayTopicService payTopicService;
    @Resource
    private IPayProductService payProductService;

    @Resource
    private QiyuCurrencyAccountService qiyuCurrencyAccountService;
    
    @Resource
    MQProducer mqProducer;

    private static final Logger LOGGER = LoggerFactory.getLogger(PayOrderServiceImpl.class);

    private static final String REDIS_ORDER_ID_INCR_KEY_PREFIX = "payOrderId";

    @Override
    public PayOrderPO queryByOrderId(String orderId) {
        LambdaQueryWrapper<PayOrderPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PayOrderPO::getOrderId, orderId);
        queryWrapper.last("limit 1");
        return payOrderMapper.selectOne(queryWrapper);
    }

    @Override
    public String insertOne(PayOrderPO payOrderPO) {
//        String orderId = String.valueOf(redisSeqIdHelper.nextId(REDIS_ORDER_ID_INCR_KEY_PREFIX));
        payOrderPO.setOrderId(UUID.randomUUID().toString());
        payOrderMapper.insert(payOrderPO);
        return payOrderPO.getOrderId();
    }

    @Override
    public boolean updateOrderStatus(Long id, Integer status) {
        PayOrderPO payOrderPO = new PayOrderPO();
        payOrderPO.setId(id);
        payOrderPO.setStatus(status);
        return payOrderMapper.updateById(payOrderPO) > 0;
    }

    @Override
    public boolean updateOrderStatus(String orderId, Integer status) {
        PayOrderPO payOrderPO = new PayOrderPO();
        payOrderPO.setStatus(status);
        LambdaUpdateWrapper<PayOrderPO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PayOrderPO::getOrderId, orderId);
        return payOrderMapper.update(payOrderPO, updateWrapper) > 0;
    }

    @Override
    public boolean payNotify(PayOrderDTO payOrderDTO) {
        PayOrderPO payOrderPO = this.queryByOrderId(payOrderDTO.getOrderId());
        if (payOrderPO == null) {
            LOGGER.error("error payOrderPO, payOrderDTO is {}", payOrderDTO);
            return false;
        }

        PayTopicPO payTopicPO = payTopicService.getByCode(payOrderDTO.getBizCode());
        if (payTopicPO == null || StringUtils.isEmpty(payTopicPO.getTopic())) {
            LOGGER.error("error payTopicPO, payOrderDTO is {}", payOrderDTO);
            return false;
        }
        this.payNotifyHandler(payOrderPO);

        Message message = new Message();
        message.setTopic(payTopicPO.getTopic());
        message.setBody(JSON.toJSONBytes(payOrderPO));
        SendResult sendResult = null;
        try {
            sendResult = mqProducer.send(message);
            LOGGER.info("[payNotify] sendResult is {} ", sendResult);
        } catch (Exception e) {
            LOGGER.error("[payNotify] sendResult is {}, error is ", sendResult, e);
        }
        return true;
    }

    /**
     * 增加用户余额
     * @param payOrderPO
     */
    private void payNotifyHandler(PayOrderPO payOrderPO) {
        this.updateOrderStatus(payOrderPO.getOrderId(), OrderStatusEnum.PAYED.getCode());
        Integer productId = payOrderPO.getProductId();
        PayProductDTO payProductDTO = payProductService.getByProductId(productId);
        if (payProductDTO != null &&
                PayProductTypeEnum.QIYU_COIN.getCode().equals(payProductDTO.getType())) {
            Long userId = payOrderPO.getUserId();
            JSONObject jsonObject = JSON.parseObject(payProductDTO.getExtra());
            Integer num = jsonObject.getInteger("coin");
            qiyuCurrencyAccountService.incr(userId,num);

        }
    }
}
