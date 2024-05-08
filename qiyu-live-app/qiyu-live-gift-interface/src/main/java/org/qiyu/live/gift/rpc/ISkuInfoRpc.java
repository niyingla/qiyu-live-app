package org.qiyu.live.gift.rpc;

import org.qiyu.live.gift.dto.ShopCarReqDTO;
import org.qiyu.live.gift.dto.ShopCarRespDTO;
import org.qiyu.live.gift.dto.SkuDetailInfoDTO;
import org.qiyu.live.gift.dto.SkuInfoDTO;

import java.util.List;

public interface ISkuInfoRpc {


    /**
     * 根据anchorId查询skuInfoList
     */
    List<SkuInfoDTO> queryByAnchorId(Long anchorId);

    SkuDetailInfoDTO queryBySkuId(Long skuId, Long anchorId);


}
