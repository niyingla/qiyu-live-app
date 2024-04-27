package org.qiyu.live.gift.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.idea.qiyu.live.framework.redis.starter.key.GiftProviderCacheKeyBuilder;
import org.qiyu.live.common.interfaces.ConvertBeanUtils;
import org.qiyu.live.common.interfaces.enums.CommonStatusEum;
import org.qiyu.live.gift.dto.GiftConfigDTO;
import org.qiyu.live.gift.provider.dao.mapper.GiftConfigMapper;
import org.qiyu.live.gift.provider.dao.po.GiftConfigPO;
import org.qiyu.live.gift.provider.service.IGiftConfigService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class GiftConfigServiceImpl implements IGiftConfigService {
    @Resource
    GiftConfigMapper giftConfigMapper;

    @Resource
    GiftProviderCacheKeyBuilder cacheKeyBuilder;

    @Resource
    RedisTemplate<String,Object> redisTemplate;


    @Override
    public GiftConfigDTO getByGiftId(Integer giftId) {
        LambdaQueryWrapper<GiftConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GiftConfigPO::getGiftId, giftId);
        queryWrapper.eq(GiftConfigPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        GiftConfigPO giftConfigPO = giftConfigMapper.selectOne(queryWrapper);
        return ConvertBeanUtils.convert(giftConfigPO, GiftConfigDTO.class);
    }

    @Override
    public List<GiftConfigDTO> queryGiftList() {
        String cacheKey = cacheKeyBuilder.buildGiftListCacheKey();
        //礼物的列表数据不会特别多，直接进行list的全量便利
        List<GiftConfigDTO> cacheList = redisTemplate.opsForList().range(cacheKey, 0, 100).stream()
                .map(x -> (GiftConfigDTO) x).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(cacheList)) {
            //不是空list缓存
            if (cacheList.get(0).getGiftId() != null) {
                redisTemplate.expire(cacheKey, 60, TimeUnit.MINUTES);
                return cacheList;
            }
            return Collections.emptyList();
        }
        //如果为空：一种是空值缓存（放了一个空的list集合），另一种是缓存过期了
        //list集合去进行存放
        LambdaQueryWrapper<GiftConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GiftConfigPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        List<GiftConfigPO> giftConfigPOList = giftConfigMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(giftConfigPOList)) {
            List<GiftConfigDTO> resultList = ConvertBeanUtils.convertList(giftConfigPOList, GiftConfigDTO.class);
            boolean trySetToRedis = redisTemplate.opsForValue().setIfAbsent(cacheKeyBuilder.buildGiftListLockCacheKey(),1,3,TimeUnit.SECONDS);
            if(trySetToRedis) {
                redisTemplate.opsForList().leftPushAll(cacheKey, resultList.toArray());
                //大部分情况下，一个直播间的有效时间大概就是60min以上
                redisTemplate.expire(cacheKey, 60, TimeUnit.MINUTES);
            }
            return resultList;
        }
        //存入一个空的list进入redis中
        redisTemplate.opsForList().leftPush(cacheKey, new GiftConfigDTO());
        redisTemplate.expire(cacheKey, 5, TimeUnit.MINUTES);
        return Collections.emptyList();
    }

    @Override
    public void insertOne(GiftConfigDTO giftConfigDTO) {
        GiftConfigPO giftConfigPO = ConvertBeanUtils.convert(giftConfigDTO, GiftConfigPO.class);
        giftConfigPO.setStatus(CommonStatusEum.VALID_STATUS.getCode());
        giftConfigMapper.insert(giftConfigPO);

    }

    @Override
    public void updateOne(GiftConfigDTO giftConfigDTO) {
        GiftConfigPO giftConfigPO = ConvertBeanUtils.convert(giftConfigDTO, GiftConfigPO.class);
        giftConfigMapper.updateById(giftConfigPO);
    }
}
