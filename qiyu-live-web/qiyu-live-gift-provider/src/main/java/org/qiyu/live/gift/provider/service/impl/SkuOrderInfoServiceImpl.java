package org.qiyu.live.gift.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.qiyu.live.common.interfaces.ConvertBeanUtils;
import org.qiyu.live.gift.dto.SkuOrderInfoReqDTO;
import org.qiyu.live.gift.dto.SkuOrderInfoRespDTO;
import org.qiyu.live.gift.provider.dao.mapper.ISkuOrderInfoMapper;
import org.qiyu.live.gift.provider.dao.po.SkuOrderInfoPO;
import org.qiyu.live.gift.provider.service.ISkuOrderInfoService;
import org.springframework.stereotype.Service;

@Service
public class SkuOrderInfoServiceImpl implements ISkuOrderInfoService {
    @Resource
    private ISkuOrderInfoMapper skuOrderInfoMapper;

    @Override
    public SkuOrderInfoRespDTO queryByUserIdAndRoomId(Long userId, Integer roomId) {
        LambdaQueryWrapper<SkuOrderInfoPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuOrderInfoPO::getUserId, userId);
        queryWrapper.eq(SkuOrderInfoPO::getRoomId, roomId);
        queryWrapper.orderByDesc(SkuOrderInfoPO::getId);
        queryWrapper.last("limit 1");
        return ConvertBeanUtils.convert(skuOrderInfoMapper.selectOne(queryWrapper), SkuOrderInfoRespDTO.class);
    }

    @Override
    public SkuOrderInfoPO insertOne(SkuOrderInfoReqDTO skuOrderInfoReqDTO) {
        String skuIdListStr = StringUtils.join( skuOrderInfoReqDTO.getSkuIdList(),",");
        SkuOrderInfoPO skuOrderInfoPO = ConvertBeanUtils.convert(skuOrderInfoReqDTO, SkuOrderInfoPO.class);
        skuOrderInfoPO.setSkuIdList(skuIdListStr);
        skuOrderInfoMapper.insert(skuOrderInfoPO);
        return skuOrderInfoPO;
    }

    @Override
    public boolean updateOrderStatus(SkuOrderInfoReqDTO skuOrderInfoReqDTO) {
        SkuOrderInfoPO skuOrderInfoPO = new SkuOrderInfoPO();
        skuOrderInfoPO.setStatus(skuOrderInfoReqDTO.getStatus());
        skuOrderInfoPO.setId(skuOrderInfoReqDTO.getId());
        skuOrderInfoMapper.updateById(skuOrderInfoPO);
        return true;
    }

    @Override
    public SkuOrderInfoRespDTO queryByOrderId(Long orderId) {
        return ConvertBeanUtils.convert(skuOrderInfoMapper.selectById(orderId), SkuOrderInfoRespDTO.class);
    }
}
