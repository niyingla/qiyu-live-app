package org.qiyu.live.api.service.impl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.api.service.IShopInfoService;
import org.qiyu.live.api.vo.req.PrepareOrderVO;
import org.qiyu.live.api.vo.req.ShopCarReqVO;
import org.qiyu.live.api.vo.req.SkuInfoReqVO;
import org.qiyu.live.api.vo.resp.ShopCarRespVO;
import org.qiyu.live.api.vo.resp.SkuDetailInfoVO;
import org.qiyu.live.api.vo.resp.SkuInfoVO;
import org.qiyu.live.common.interfaces.ConvertBeanUtils;
import org.qiyu.live.gift.dto.PrepareOrderReqDTO;
import org.qiyu.live.gift.dto.ShopCarReqDTO;
import org.qiyu.live.gift.dto.ShopCarRespDTO;
import org.qiyu.live.gift.dto.SkuPrepareOrderInfoDTO;
import org.qiyu.live.gift.rpc.IShopCarRpc;
import org.qiyu.live.gift.rpc.ISkuInfoRpc;
import org.qiyu.live.gift.rpc.ISkuOrderInfoRpc;
import org.qiyu.live.gift.rpc.ISkuStockInfoRpc;
import org.qiyu.live.living.interfaces.dto.LivingRoomRespDTO;
import org.qiyu.live.living.interfaces.rpc.ILivingRoomRpc;
import org.qiyu.live.web.starter.context.QiyuRequestContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopInfoServiceImpl implements IShopInfoService {

    @DubboReference
    private ILivingRoomRpc livingRoomRpc;
    @DubboReference
    private ISkuInfoRpc skuInfoRpc;

    @DubboReference
    private IShopCarRpc shopCarRpc;

    @DubboReference
    private ISkuOrderInfoRpc skuOrderInfoRpc;

    @DubboReference
    private ISkuStockInfoRpc skuStockInfoRpc;

    @Override
    public List<SkuInfoVO> queryByRoomId(Integer roomId) {
        System.out.println("queryByAnchorId");
        LivingRoomRespDTO respDTO = livingRoomRpc.queryByRoomId(roomId);
        Long anchorId = respDTO.getAnchorId();
        System.out.println("anchorId="+anchorId);
        return ConvertBeanUtils.convertList(skuInfoRpc.queryByAnchorId(anchorId),SkuInfoVO.class);
    }

    @Override
    public SkuDetailInfoVO detail(SkuInfoReqVO skuInfoReqVO) {
        return ConvertBeanUtils.convert(skuInfoRpc.queryBySkuId(skuInfoReqVO.getSkuId(),skuInfoReqVO.getAnchorId()),SkuDetailInfoVO.class);
    }

    @Override
    public Boolean addCar(ShopCarReqVO reqVO) {
        ShopCarReqDTO reqDTO = ConvertBeanUtils.convert(reqVO, ShopCarReqDTO.class);
        reqDTO.setUserId(QiyuRequestContext.getUserId());
        return shopCarRpc.addCar(reqDTO);
    }

    @Override
    public Boolean removeFromCar(ShopCarReqVO reqVO) {
        ShopCarReqDTO reqDTO = ConvertBeanUtils.convert(reqVO, ShopCarReqDTO.class);
        reqDTO.setUserId(QiyuRequestContext.getUserId());
        return shopCarRpc.removeFromCar(reqDTO);
    }

    @Override
    public Boolean clearShopCar(ShopCarReqVO reqVO) {
        ShopCarReqDTO reqDTO = ConvertBeanUtils.convert(reqVO, ShopCarReqDTO.class);
        reqDTO.setUserId(QiyuRequestContext.getUserId());
        return shopCarRpc.clearShopCar(reqDTO);
    }

    @Override
    public Boolean addCarItemNum(ShopCarReqVO reqVO) {
        ShopCarReqDTO reqDTO = ConvertBeanUtils.convert(reqVO, ShopCarReqDTO.class);
        reqDTO.setUserId(QiyuRequestContext.getUserId());
        return shopCarRpc.addCarItemNum(reqDTO);
    }

    @Override
    public ShopCarRespVO getCarInfo(ShopCarReqVO reqVO) {
        ShopCarReqDTO reqDTO = ConvertBeanUtils.convert(reqVO, ShopCarReqDTO.class);
        reqDTO.setUserId(QiyuRequestContext.getUserId());
        ShopCarRespDTO carInfo = shopCarRpc.getCarInfo(reqDTO);
        if(carInfo==null) return null;
        ShopCarRespVO convert = ConvertBeanUtils.convert(carInfo, ShopCarRespVO.class);
        convert.setShopCarItemRespDTOS(carInfo.getSkuCarItemRespDTODTOS());
        return convert;
    }

    @Override
    public SkuPrepareOrderInfoDTO prepareOrder(PrepareOrderVO prepareOrderVO) {
        PrepareOrderReqDTO reqDTO = new PrepareOrderReqDTO();
        reqDTO.setRoomId(prepareOrderVO.getRoomId());
        reqDTO.setUserId(QiyuRequestContext.getUserId());
        return skuOrderInfoRpc.prepareOrder(reqDTO);
    }

    @Override
    public boolean prepareStock(Long anchorId) {
        return skuStockInfoRpc.prepareStockInfo(anchorId);
    }

    @Override
    public boolean payNow(PrepareOrderVO prepareOrderVO) {
        return skuOrderInfoRpc.payNow(QiyuRequestContext.getUserId(), prepareOrderVO.getRoomId());
    }
}
