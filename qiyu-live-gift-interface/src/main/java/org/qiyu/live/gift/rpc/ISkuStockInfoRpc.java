package org.qiyu.live.gift.rpc;

public interface ISkuStockInfoRpc {

    /**
     * 根据stuId更新库存值
     * @param skuId
     * @param num
     * @return
     */
    boolean decrStockNumBySkuId(Long skuId, Integer num);

    /**
     *
     * @param anchorId
     * @return
     */
    boolean prepareStockInfo(Long anchorId);

    /**
     * 从Redis中查询缓存的库存值
     * @param skuId
     * @return
     */
    Integer queryStockNum(Long skuId);

    /**
     * 同步库存数据到MySQL
     * @param anchor
     * @return
     */
    boolean syncStockNumToMySQL(Long anchor);

    /**
     * 使用lua脚本扣减缓存的库存值
     */
    boolean decrStockNumBySkuIdByLua(Long skuId, Integer num);
}