package org.qiyu.live.api.service.impl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.api.service.ILivingRoomService;
import org.qiyu.live.api.vo.LivingRoomInitVO;
import org.qiyu.live.api.vo.req.LivingRoomReqVO;
import org.qiyu.live.api.vo.resp.LivingRoomPageRespVO;
import org.qiyu.live.api.vo.resp.LivingRoomRespVO;
import org.qiyu.live.common.interfaces.ConvertBeanUtils;
import org.qiyu.live.common.interfaces.dto.PageWrapper;
import org.qiyu.live.living.interfaces.dto.LivingRoomReqDTO;
import org.qiyu.live.living.interfaces.dto.LivingRoomRespDTO;
import org.qiyu.live.living.interfaces.rpc.ILivingRoomRpc;
import org.qiyu.live.user.dto.UserDTO;
import org.qiyu.live.user.interfaces.rpc.IUserRpc;
import org.qiyu.live.web.starter.context.QiyuRequestContext;
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

    @Override
    public LivingRoomPageRespVO list(LivingRoomReqVO livingRoomReqVO) {
        PageWrapper<LivingRoomRespDTO> resultPage = livingRoomRpc.list(ConvertBeanUtils.convert(livingRoomReqVO,LivingRoomReqDTO.class));
        LivingRoomPageRespVO livingRoomPageRespVO = new LivingRoomPageRespVO();
        livingRoomPageRespVO.setList(ConvertBeanUtils.convertList(resultPage.getList(), LivingRoomRespVO.class));
        livingRoomPageRespVO.setHasNext(resultPage.isHasNext());
        return livingRoomPageRespVO;
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

    @Override
    public LivingRoomInitVO anchorConfig(Long userId, Integer roomId) {
        LivingRoomRespDTO respDTO = livingRoomRpc.queryByRoomId(roomId);
        UserDTO userDTO = userRpc.getByUserId(userId);
        LivingRoomInitVO respVO = new LivingRoomInitVO();
        respVO.setUserId(userId);
        respVO.setNickName(userDTO.getNickName());
        if (respDTO == null || respDTO.getAnchorId() == null || userId == null) {
            respVO.setAnchor(false);
            respVO.setRoomId(-1);
        }else {
            respVO.setAnchor(respDTO.getAnchorId().equals(userId));
            respVO.setAnchorId(respDTO.getAnchorId());
            respVO.setRoomId(respDTO.getId());
        }
        return respVO;
    }
//    @Override
//    public LivingRoomInitVO anchorConfig(Long userId, Integer roomId) {
//        LivingRoomRespDTO respDTO = livingRoomRpc.queryByRoomId(roomId);
//        ErrorAssert.isNotNull(respDTO,ApiErrorEnum.LIVING_ROOM_END);
//        Map<Long,UserDTO> userDTOMap = userRpc.batchQueryUserInfo(Arrays.asList(respDTO.getAnchorId(),userId).stream().distinct().collect(Collectors.toList()));
//        UserDTO anchor = userDTOMap.get(respDTO.getAnchorId());
//        UserDTO watcher = userDTOMap.get(userId);
//        LivingRoomInitVO respVO = new LivingRoomInitVO();
//        respVO.setAnchorNickName(anchor.getNickName());
//        respVO.setWatcherNickName(watcher.getNickName());
//        respVO.setUserId(userId);
//        //给定一个默认的头像
//        respVO.setAvatar(StringUtils.isEmpty(anchor.getAvatar())?"https://s1.ax1x.com/2022/12/18/zb6q6f.png":anchor.getAvatar());
//        respVO.setWatcherAvatar(watcher.getAvatar());
//        if (respDTO == null || respDTO.getAnchorId() == null || userId == null) {
//            //这种就是属于直播间已经不存在的情况了
//            respVO.setRoomId(-1);
//        } else {
//            respVO.setRoomId(respDTO.getId());
//            respVO.setAnchorId(respDTO.getAnchorId());
//            respVO.setAnchor(respDTO.getAnchorId().equals(userId));
//        }
//        respVO.setDefaultBgImg("https://picst.sunbangyan.cn/2023/08/29/waxzj0.png");
//        return respVO;
//    }

    @Override
    public Integer startLivingRoom(LivingRoomReqDTO livingRoomReqDTO) {
        return null;
    }


}
