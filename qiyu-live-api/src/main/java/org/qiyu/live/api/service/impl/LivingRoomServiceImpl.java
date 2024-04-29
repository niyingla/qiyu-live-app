package org.qiyu.live.api.service.impl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.api.error.QiyuApiError;
import org.qiyu.live.api.service.ILivingRoomService;
import org.qiyu.live.api.vo.LivingRoomInitVO;
import org.qiyu.live.api.vo.req.LivingRoomReqVO;
import org.qiyu.live.api.vo.req.OnlinePKReqVO;
import org.qiyu.live.api.vo.resp.LivingRoomPageRespVO;
import org.qiyu.live.api.vo.resp.LivingRoomRespVO;
import org.qiyu.live.api.vo.resp.RedPacketReceiveVO;
import org.qiyu.live.common.interfaces.ConvertBeanUtils;
import org.qiyu.live.common.interfaces.dto.PageWrapper;
import org.qiyu.live.gift.dto.RedPacketConfigReqDTO;
import org.qiyu.live.gift.dto.RedPacketConfigRespDTO;
import org.qiyu.live.gift.dto.RedPacketReceiveDTO;
import org.qiyu.live.gift.rpc.IRedPacketConfigRpc;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.living.interfaces.dto.LivingPkRespDTO;
import org.qiyu.live.living.interfaces.dto.LivingRoomReqDTO;
import org.qiyu.live.living.interfaces.dto.LivingRoomRespDTO;
import org.qiyu.live.living.interfaces.rpc.ILivingRoomRpc;
import org.qiyu.live.user.dto.UserDTO;
import org.qiyu.live.user.interfaces.rpc.IUserRpc;
import org.qiyu.live.web.starter.context.QiyuRequestContext;
import org.qiyu.live.web.starter.error.BizBaseErrorEnum;
import org.qiyu.live.web.starter.error.ErrorAssert;
import org.qiyu.live.web.starter.error.QiyuErrorException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LivingRoomServiceImpl implements ILivingRoomService {
    @DubboReference
    ILivingRoomRpc livingRoomRpc;

    @DubboReference
    private IUserRpc userRpc;

    @DubboReference
    private IRedPacketConfigRpc redPacketConfigRpc;

    @Override
    public LivingRoomPageRespVO list(LivingRoomReqVO livingRoomReqVO) {
        PageWrapper<LivingRoomRespDTO> resultPage = livingRoomRpc.list(ConvertBeanUtils.convert(livingRoomReqVO,LivingRoomReqDTO.class));
        LivingRoomPageRespVO livingRoomPageRespVO = new LivingRoomPageRespVO();
        livingRoomPageRespVO.setList(ConvertBeanUtils.convertList(resultPage.getList(), LivingRoomRespVO.class));
        livingRoomPageRespVO.setHasNext(resultPage.isHasNext());
        return livingRoomPageRespVO;
    }

    @Override
    public boolean onlinePK(OnlinePKReqVO onlinePKReqVO) {
        LivingRoomReqDTO reqDTO = ConvertBeanUtils.convert(onlinePKReqVO,LivingRoomReqDTO.class);
        reqDTO.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
        reqDTO.setPkObjId(QiyuRequestContext.getUserId());
        LivingPkRespDTO tryOnlineStatus = livingRoomRpc.onlinePK(reqDTO);
        ErrorAssert.isTure(tryOnlineStatus.isOnlineStatus(), new QiyuErrorException(-1,tryOnlineStatus.getMsg()));
        return true;
    }

    @Override
    public Boolean prepareRedPacket(Long userId, Integer roomId) {
        LivingRoomRespDTO livingRoomRespDTO = livingRoomRpc.queryByRoomId(roomId);
        System.out.println(livingRoomRespDTO+"userid is"+userId+"roomid is "+roomId);
        ErrorAssert.isNotNull(livingRoomRespDTO, BizBaseErrorEnum.PARAM_ERROR);
        ErrorAssert.isTure(userId.equals(livingRoomRespDTO.getAnchorId()), BizBaseErrorEnum.PARAM_ERROR);
        System.out.println("进入rpc");
        return redPacketConfigRpc.prepareRedPacket(userId);
    }

    @Override
    public Boolean startRedPacket(Long userId, String code) {
        RedPacketConfigReqDTO reqDTO = new RedPacketConfigReqDTO();
        reqDTO.setUserId(userId);
        reqDTO.setRedPacketConfigCode(code);
        LivingRoomRespDTO respDTO = livingRoomRpc.queryByAnchorId(userId);
        System.out.println(respDTO);
        System.out.println(userId+"--------code is"+code);
        ErrorAssert.isNotNull(respDTO,BizBaseErrorEnum.PARAM_ERROR);
        reqDTO.setRoomId(respDTO.getId());
        return redPacketConfigRpc.startRedPacket(reqDTO);
    }

    @Override
    public RedPacketReceiveVO getRedPacket(Long userId, String redPacketConfigCode) {
        RedPacketConfigReqDTO redPacketConfigReqDTO = new RedPacketConfigReqDTO();
        redPacketConfigReqDTO.setUserId(userId);
        redPacketConfigReqDTO.setRedPacketConfigCode(redPacketConfigCode);
        RedPacketReceiveDTO receiveDTO = redPacketConfigRpc.receiveRedPacket(redPacketConfigReqDTO);
        RedPacketReceiveVO redPacketReceiveVO = new RedPacketReceiveVO();
        if(receiveDTO==null){
            redPacketReceiveVO.setMsg("红包派发完毕");
        }else{
            redPacketReceiveVO.setPrice(receiveDTO.getPrice());
            redPacketReceiveVO.setMsg(receiveDTO.getNotifyMsg());
        }
        return redPacketReceiveVO;
    }


    @Override
    public Integer startingLiving(Integer type) {
        Long userId = QiyuRequestContext.getUserId();
        UserDTO userDTO = userRpc.getByUserId(userId);
        LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
        livingRoomReqDTO.setAnchorId(userId);
        livingRoomReqDTO.setRoomName("主播-" + userId + "的直播间");
        livingRoomReqDTO.setCovertImg(userDTO.getAvatar());
        livingRoomReqDTO.setType(type);
        return livingRoomRpc.startLivingRoom(livingRoomReqDTO);
    }

    @Override
    public boolean closeLiving(Integer roomId) {
        LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
        livingRoomReqDTO.setRoomId(roomId);
        livingRoomReqDTO.setAnchorId(QiyuRequestContext.getUserId());
        return livingRoomRpc.closeLiving(livingRoomReqDTO);
    }

//    @Override
//    public LivingRoomInitVO anchorConfig(Long userId, Integer roomId) {
//        LivingRoomRespDTO respDTO = livingRoomRpc.queryByRoomId(roomId);
//        UserDTO userDTO = userRpc.getByUserId(userId);
//        LivingRoomInitVO respVO = new LivingRoomInitVO();
//        respVO.setUserId(userId);
//        respVO.setNickName(userDTO.getNickName());
//        if (respDTO == null || respDTO.getAnchorId() == null || userId == null) {
//            respVO.setAnchor(false);
//            respVO.setRoomId(-1);
//        }else {
//            respVO.setAnchor(respDTO.getAnchorId().equals(userId));
//            respVO.setAnchorId(respDTO.getAnchorId());
//            respVO.setRoomId(respDTO.getId());
//        }
//        return respVO;
//    }
    @Override
    public LivingRoomInitVO anchorConfig(Long userId, Integer roomId) {
        LivingRoomRespDTO respDTO = livingRoomRpc.queryByRoomId(roomId);
        ErrorAssert.isNotNull(respDTO, QiyuApiError.LIVING_ROOM_END);
        Map<Long,UserDTO> userDTOMap = userRpc.batchQueryUserInfo(Arrays.asList(respDTO.getAnchorId(),userId).stream().distinct().collect(Collectors.toList()));
        UserDTO anchor = userDTOMap.get(respDTO.getAnchorId());
        UserDTO watcher = userDTOMap.get(userId);
        LivingRoomInitVO respVO = new LivingRoomInitVO();
        respVO.setAnchorNickName(anchor.getNickName());
        respVO.setWatcherNickName(watcher.getNickName());
        respVO.setUserId(userId);
        //给定一个默认的头像
        respVO.setAvatar(StringUtils.isEmpty(anchor.getAvatar())?"https://s1.ax1x.com/2022/12/18/zb6q6f.png":anchor.getAvatar());
        respVO.setWatcherAvatar(watcher.getAvatar());
        respVO.setDefaultBgImg("https://s1.ax1x.com/2022/12/18/zb6q6f.png");
        if (respDTO == null || respDTO.getAnchorId() == null || userId == null) {
            //这种就是属于直播间已经不存在的情况了
            respVO.setRoomId(-1);
            return respVO;
        } else {
            boolean isAnchorId = respDTO.getAnchorId().equals(userId);
            respVO.setRoomId(respDTO.getId());
            respVO.setAnchorId(respDTO.getAnchorId());
            respVO.setAnchor(isAnchorId);
            if(isAnchorId){
                RedPacketConfigRespDTO redPacketConfigRespDTO = redPacketConfigRpc.queryByAnchorId(userId);
                if(redPacketConfigRespDTO!=null){
                    respVO.setRedPacketConfigCode(redPacketConfigRespDTO.getConfigCode());
                }
            }
        }

        return respVO;
    }

    @Override
    public Integer startLivingRoom(LivingRoomReqDTO livingRoomReqDTO) {
        return null;
    }


}
