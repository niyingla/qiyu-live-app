package org.qiyu.live.gift.provider.rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.idea.qiyu.live.framework.redis.starter.key.GiftProviderCacheKeyBuilder;
import org.qiyu.live.common.interfaces.ConvertBeanUtils;
import org.qiyu.live.gift.dto.ShopCarReqDTO;
import org.qiyu.live.gift.dto.ShopCarRespDTO;
import org.qiyu.live.gift.dto.SkuDetailInfoDTO;
import org.qiyu.live.gift.dto.SkuInfoDTO;
import org.qiyu.live.gift.provider.dao.po.SkuInfoPO;
import org.qiyu.live.gift.provider.service.IAnchorShopInfoService;
import org.qiyu.live.gift.provider.service.ISkuInfoService;
import org.qiyu.live.gift.rpc.ISkuInfoRpc;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
@DubboService
public class SkuInfoRpcImpl implements ISkuInfoRpc {
    @Resource
    ISkuInfoService skuInfoService;

    @Resource
    IAnchorShopInfoService anchorShopInfoService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private GiftProviderCacheKeyBuilder cacheKeyBuilder;

    @Override
    public List<SkuInfoDTO> queryByAnchorId(Long anchorId) {
        List<Long> ids = anchorShopInfoService.querySkuIdsByAnchorId(anchorId);
        List<SkuInfoPO> skuInfoPOS = skuInfoService.queryBySkuIds(ids);
        return ConvertBeanUtils.convertList(skuInfoPOS,SkuInfoDTO.class);
    }

    @Override
    public SkuDetailInfoDTO queryBySkuId(Long skuId, Long anchorId) {
        String cacheKey = cacheKeyBuilder.buildSkuDetailInfoMap(anchorId);
        SkuInfoDTO skuInfoDTO = (SkuInfoDTO) redisTemplate.opsForHash().get(cacheKey, String.valueOf(skuId));
        if (skuInfoDTO != null) {
            return ConvertBeanUtils.convert(skuInfoDTO, SkuDetailInfoDTO.class);
        }
        skuInfoDTO = ConvertBeanUtils.convert(skuInfoService.queryBySkuId(skuId), SkuInfoDTO.class);
        if (skuInfoDTO != null) {
            redisTemplate.opsForHash().put(cacheKey, String.valueOf(skuId), skuInfoDTO);
        }
        return ConvertBeanUtils.convert(skuInfoDTO, SkuDetailInfoDTO.class);
    }

}
