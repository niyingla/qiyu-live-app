package org.qiyu.live.api.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.api.service.IBankService;
import org.qiyu.live.api.vo.PayProductReqVO;
import org.qiyu.live.api.vo.PayProductRespVO;
import org.qiyu.live.api.vo.resp.PayProductItemVO;
import org.qiyu.live.api.vo.resp.PayProductVO;
import org.qiyu.live.bank.constants.OrderStatusEnum;
import org.qiyu.live.bank.constants.PaySourceEnum;
import org.qiyu.live.bank.dto.PayOrderDTO;
import org.qiyu.live.bank.dto.PayProductDTO;
import org.qiyu.live.bank.rpc.IPayOrderRpc;
import org.qiyu.live.bank.rpc.IPayProductRpc;
import org.qiyu.live.bank.rpc.QiyuCurrencyAccountRpc;
import org.qiyu.live.web.starter.context.QiyuRequestContext;
import org.qiyu.live.web.starter.error.BizBaseErrorEnum;
import org.qiyu.live.web.starter.error.ErrorAssert;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class BankServiceImpl implements IBankService {
    
    @DubboReference
    private IPayProductRpc payProductRpc;
    @DubboReference
    private QiyuCurrencyAccountRpc qiyuCurrencyAccountRpc;
    @DubboReference
    private IPayOrderRpc payOrderRpc;

    @Resource
    private RestTemplate restTemplate;

    @Override
    public PayProductVO products(Integer type) {
        List<PayProductDTO> payProductDTOS = payProductRpc.products(type);
        List<PayProductItemVO> payProductItemVOS = new ArrayList<>();
        for (PayProductDTO payProductDTO : payProductDTOS) {
            PayProductItemVO payProductItemVO = new PayProductItemVO();
            payProductItemVO.setId(payProductDTO.getId());
            payProductItemVO.setName(payProductDTO.getName());
            payProductItemVO.setCoinNum(JSON.parseObject(payProductDTO.getExtra()).getInteger("coin"));
            payProductItemVOS.add(payProductItemVO);
        }
        PayProductVO payProductVO = new PayProductVO();
        payProductVO.setCurrentBalance(qiyuCurrencyAccountRpc.getBalance(QiyuRequestContext.getUserId()));
        payProductVO.setPayProductItemVOList(payProductItemVOS);
        return payProductVO;
    }

    @Override
    public PayProductRespVO payProduct(PayProductReqVO payProductReqVO) {
        // 参数校验
        ErrorAssert.isTure(payProductReqVO != null && payProductReqVO.getProductId() != null && payProductReqVO.getPaySource() != null, BizBaseErrorEnum.PARAM_ERROR);
        ErrorAssert.isNotNull(PaySourceEnum.find(payProductReqVO.getPaySource()), BizBaseErrorEnum.PARAM_ERROR);
        // 查询payProductDTO
        PayProductDTO payProductDTO = payProductRpc.getByProductId(payProductReqVO.getProductId());
        ErrorAssert.isNotNull(payProductDTO, BizBaseErrorEnum.PARAM_ERROR);

        // 生成一条订单（待支付状态）
        PayOrderDTO payOrderDTO = new PayOrderDTO();
        payOrderDTO.setProductId(payProductReqVO.getProductId());
        payOrderDTO.setUserId(QiyuRequestContext.getUserId());
        payOrderDTO.setPayTime(new Date());
        payOrderDTO.setSource(payProductReqVO.getPaySource());
        payOrderDTO.setPayChannel(payProductReqVO.getPayChannel());
        String orderId = payOrderRpc.insertOne(payOrderDTO);
        // 模拟点击 去支付 按钮，更新订单状态为 支付中
        payOrderRpc.updateOrderStatus(orderId, OrderStatusEnum.PAYING.getCode());
        PayProductRespVO payProductRespVO = new PayProductRespVO();
        payProductRespVO.setOrderId(orderId);

        //todo 远程http请求 resttemplate-》支付回调接口
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("orderId", orderId);
        jsonObject.put("userId", QiyuRequestContext.getUserId());
        jsonObject.put("bizCode", 10001);
        HashMap<String,String> paramMap = new HashMap<>();
        paramMap.put("param",jsonObject.toJSONString());
        ResponseEntity<String> resultEntity = restTemplate.postForEntity("http://localhost:8201/live/bank/payNotify/wxNotify?param={param}", null, String.class,paramMap);
        return payProductRespVO;
    }



}