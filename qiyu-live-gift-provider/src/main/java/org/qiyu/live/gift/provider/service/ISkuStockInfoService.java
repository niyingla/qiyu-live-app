package org.qiyu.live.gift.provider.service;

import org.qiyu.live.gift.bo.RollBackStockBO;
import org.qiyu.live.gift.dto.RollBackStockDTO;
import org.qiyu.live.gift.provider.dao.po.SkuStockInfoPO;

import java.util.List;

public interface ISkuStockInfoService {

    /**
     * 根据skuId更新库存值
     */
    boolean updateStockNum(Long skuId, Integer stockNum);

    /**
     * 根据stuId扣减库存值
     */
    boolean decrStockNumBySkuId(Long skuId, Integer num);

    /**
     * 使用lua脚本扣减缓存的库存值
     */
    boolean decrStockNumBySkuIdByLua(Long skuId, Integer num);

    /**
     * 根据skuId查询库存值
     */
    SkuStockInfoPO queryBySkuId(Long skuId);

    /**
     * 根据stuIdList批量查询数据
     */
    List<SkuStockInfoPO> queryBySkuIds(List<Long> skuIdList);

    /**
     * 库存回滚
     */
    void stockRollBackHandler(RollBackStockDTO rollBackStockDTO);
    
}