package org.qiyu.live.api.controller;

import jakarta.annotation.Resource;
import org.qiyu.live.api.service.ILivingRoomService;
import org.qiyu.live.api.vo.LivingRoomInitVO;
import org.qiyu.live.api.vo.req.LivingRoomReqVO;
import org.qiyu.live.api.vo.req.OnlinePKReqVO;
import org.qiyu.live.common.interfaces.vo.WebResponseVO;
import org.qiyu.live.web.starter.context.QiyuRequestContext;
import org.qiyu.live.web.starter.error.BizBaseErrorEnum;
import org.qiyu.live.web.starter.error.ErrorAssert;
import org.qiyu.live.web.starter.limit.RequestLimit;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/living")
public class LivingRoomController {

    @Resource
    private ILivingRoomService livingRoomService;

    @PostMapping("/list")
    public WebResponseVO list(LivingRoomReqVO livingRoomReqVO) {
        ErrorAssert.isTure(livingRoomReqVO != null && livingRoomReqVO.getType() != null, BizBaseErrorEnum.PARAM_ERROR);
        ErrorAssert.isTure(livingRoomReqVO.getPage() > 0 && livingRoomReqVO.getPageSize() <= 100, BizBaseErrorEnum.PARAM_ERROR);
        return WebResponseVO.success(livingRoomService.list(livingRoomReqVO));
    }

    @RequestLimit(limit = 1, second = 10, msg = "开播请求过于频繁，请稍后再试")
    @PostMapping("/startingLiving")
    public WebResponseVO startingLiving(Integer type) {
        ErrorAssert.isNotNull(type, BizBaseErrorEnum.PARAM_ERROR);
        Integer roomId = livingRoomService.startingLiving(type);
        LivingRoomInitVO livingRoomInitVO = new LivingRoomInitVO();
        livingRoomInitVO.setRoomId(roomId);
        return WebResponseVO.success(livingRoomInitVO);
    }
    @PostMapping("/onlinePk")
    @RequestLimit(limit = 1, second = 3, msg = "请求过于频繁，请稍后再试")
    public WebResponseVO onlinePk(OnlinePKReqVO onlinePKReqVO){
        ErrorAssert.isNotNull(onlinePKReqVO.getRoomId(), BizBaseErrorEnum.PARAM_ERROR);
        return WebResponseVO.success(livingRoomService.onlinePK(onlinePKReqVO));
    }
    @RequestLimit(limit = 1, second = 10, msg = "关播请求过于频繁，请稍后再试")
    @PostMapping("/closeLiving")
    public WebResponseVO closeLiving(Integer roomId) {
        ErrorAssert.isNotNull(roomId, BizBaseErrorEnum.PARAM_ERROR);
        boolean status = livingRoomService.closeLiving(roomId);
        if (status) {
            return WebResponseVO.success();
        }
        return WebResponseVO.bizError("关播异常，请稍后再试");
    }

    @PostMapping("/anchorConfig")
    public WebResponseVO anchorConfig(Integer roomId) {
        Long userId = QiyuRequestContext.getUserId();
        WebResponseVO success = WebResponseVO.success(livingRoomService.anchorConfig(userId, roomId));
        return success;
    }
    @RequestLimit(limit = 1, second = 10, msg = "正在初始化红包数据，请稍等")
    @PostMapping("/prepareRedPacket")
    public WebResponseVO prepareRedPacket(LivingRoomReqVO livingRoomReqVO) {
        return WebResponseVO.success(livingRoomService.prepareRedPacket(QiyuRequestContext.getUserId(), livingRoomReqVO.getRoomId()));
    }

    @RequestLimit(limit = 1, second = 10, msg = "正在广播直播间用户，请稍等")
    @PostMapping("/startRedPacket")
    public WebResponseVO startRedPacket(LivingRoomReqVO livingRoomReqVO) {
        return WebResponseVO.success(livingRoomService.startRedPacket(QiyuRequestContext.getUserId(), livingRoomReqVO.getRedPacketConfigCode()));
    }

    @RequestLimit(limit = 1, second = 3, msg = "手速太快了，请稍等")
    @PostMapping("/getRedPacket")
    public WebResponseVO getRedPacket(LivingRoomReqVO livingRoomReqVO) {
        return WebResponseVO.success(livingRoomService.getRedPacket(QiyuRequestContext.getUserId(), livingRoomReqVO.getRedPacketConfigCode()));
    }

}