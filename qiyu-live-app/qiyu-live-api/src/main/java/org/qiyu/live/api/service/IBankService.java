package org.qiyu.live.api.service;

import org.qiyu.live.api.vo.PayProductReqVO;
import org.qiyu.live.api.vo.PayProductRespVO;
import org.qiyu.live.api.vo.resp.PayProductVO;

public interface IBankService {

    /**
     * 查询相关产品信息
     */
    PayProductVO products(Integer type);

    /**
     * 发起支付
     *
     * @param payProductReqVO
     * @return
     */
    PayProductRespVO payProduct(PayProductReqVO payProductReqVO);
}