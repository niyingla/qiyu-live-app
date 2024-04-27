package org.qiyu.live.api.service;

import org.qiyu.live.api.vo.LivingRoomInitVO;
import org.qiyu.live.api.vo.req.LivingRoomReqVO;
import org.qiyu.live.api.vo.req.OnlinePKReqVO;
import org.qiyu.live.api.vo.resp.LivingRoomPageRespVO;
import org.qiyu.live.living.interfaces.dto.LivingRoomReqDTO;
import org.qiyu.live.living.interfaces.dto.LivingRoomRespDTO;

public interface ILivingRoomService {
    /**
     * 开始直播
     */
    Integer startingLiving(Integer type);

    /**
     * 关闭直播
     */
    boolean closeLiving(Integer roomId);

    /**
     * 验证当前用户是否是主播身份
     */
    LivingRoomInitVO anchorConfig(Long userId, Integer roomId);

    Integer startLivingRoom(LivingRoomReqDTO livingRoomReqDTO);

    LivingRoomPageRespVO list(LivingRoomReqVO livingRoomReqVO);

    /**
     * 用户在pk直播间，连线请求
     * @param onlinePKReqVO
     * @return
     */
    boolean onlinePK(OnlinePKReqVO onlinePKReqVO);
}
