package org.qiyu.live.bank.provider.service;

import org.qiyu.live.bank.dto.PayOrderDTO;
import org.qiyu.live.bank.provider.dao.po.PayOrderPO;

public interface IPayOrderService {
    
    /**
     * 根据orderId查询订单信息
     */
    PayOrderPO queryByOrderId(String orderId);

    /**
     *插入订单 ，返回主键id
     */
    String insertOne(PayOrderPO payOrderPO);

    /**
     * 根据主键id更新订单状态
     */
    boolean updateOrderStatus(Long id, Integer status);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(String orderId, Integer status);

    /**
     * 支付回调需要请求该接口
     *
     * @param payOrderDTO
     * @return
     */
    boolean payNotify(PayOrderDTO payOrderDTO);
}