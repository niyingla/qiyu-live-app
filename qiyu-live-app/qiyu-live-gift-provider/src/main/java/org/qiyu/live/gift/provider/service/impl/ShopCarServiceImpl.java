package org.qiyu.live.gift.provider.service.impl;

import jakarta.annotation.Resource;
import org.idea.qiyu.live.framework.redis.starter.key.GiftProviderCacheKeyBuilder;
import org.qiyu.live.common.interfaces.ConvertBeanUtils;
import org.qiyu.live.gift.dto.ShopCarItemRespDTO;
import org.qiyu.live.gift.dto.ShopCarReqDTO;
import org.qiyu.live.gift.dto.ShopCarRespDTO;
import org.qiyu.live.gift.dto.SkuInfoDTO;
import org.qiyu.live.gift.provider.dao.po.SkuInfoPO;
import org.qiyu.live.gift.provider.service.IShopCarService;
import org.qiyu.live.gift.provider.service.ISkuInfoService;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShopCarServiceImpl implements IShopCarService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private GiftProviderCacheKeyBuilder cacheKeyBuilder;

    @Resource
    private ISkuInfoService skuInfoService;

    @Override
    public Boolean addCar(ShopCarReqDTO shopCarReqDTO) {
        String cacheKey = cacheKeyBuilder.buildUserShopCar(shopCarReqDTO.getUserId(), shopCarReqDTO.getRoomId());
        //一个用户，多个商品
        redisTemplate.opsForHash().put(cacheKey, String.valueOf(shopCarReqDTO.getSkuId()), 1);
        System.out.println("return true");
        return true;
    }

    @Override
    public Boolean removeFromCar(ShopCarReqDTO shopCarReqDTO) {
        String cacheKey = cacheKeyBuilder.buildUserShopCar(shopCarReqDTO.getUserId(), shopCarReqDTO.getRoomId());
        redisTemplate.opsForHash().delete(cacheKey,  String.valueOf(shopCarReqDTO.getSkuId()));
        return true;
    }

    @Override
    public Boolean clearShopCar(ShopCarReqDTO shopCarReqDTO) {

        String cacheKey = cacheKeyBuilder.buildUserShopCar(shopCarReqDTO.getUserId(), shopCarReqDTO.getRoomId());
        return redisTemplate.delete(cacheKey);
    }

    @Override
    public Boolean addCarItemNum(ShopCarReqDTO shopCarReqDTO) {
        String cacheKey = cacheKeyBuilder.buildUserShopCar(shopCarReqDTO.getUserId(), shopCarReqDTO.getRoomId());
        redisTemplate.opsForHash().increment(cacheKey, shopCarReqDTO.getSkuId(), 1);
        return true;
    }

    @Override
    public ShopCarRespDTO getCarInfo(ShopCarReqDTO shopCarReqDTO) {
        String cacheKey = cacheKeyBuilder.buildUserShopCar(shopCarReqDTO.getUserId(), shopCarReqDTO.getRoomId());
        Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(cacheKey, ScanOptions.scanOptions().match("*").build());
        List<ShopCarItemRespDTO> ShopCarItemRespDTOs = new ArrayList<>();
        HashMap<Long, Integer> skuCountMap = new HashMap<>();
        while (cursor.hasNext()) {
            Map.Entry<Object, Object> entry = cursor.next();
            skuCountMap.put(Long.valueOf((String) entry.getKey()), (Integer) entry.getValue());
        }
        List<SkuInfoPO> skuInfoPOS = skuInfoService.queryBySkuIds(new ArrayList<>(skuCountMap.keySet()));
        if(skuInfoPOS==null) return null;
        for (SkuInfoPO skuInfoPO : skuInfoPOS) {
            SkuInfoDTO skuInfoDTO = ConvertBeanUtils.convert(skuInfoPO, SkuInfoDTO.class);
            Integer count = skuCountMap.get(skuInfoDTO.getSkuId());
            ShopCarItemRespDTOs.add(new ShopCarItemRespDTO(count, skuInfoDTO));
        }
        ShopCarRespDTO shopCarRespDTO = new ShopCarRespDTO();
        shopCarRespDTO.setRoomId(shopCarReqDTO.getRoomId());
        shopCarRespDTO.setUserId(shopCarReqDTO.getUserId());
        shopCarRespDTO.setSkuCarItemRespDTODTOS(ShopCarItemRespDTOs);
        System.out.println("shopCarRespDTO======"+shopCarRespDTO);
        return shopCarRespDTO;
    }
}
