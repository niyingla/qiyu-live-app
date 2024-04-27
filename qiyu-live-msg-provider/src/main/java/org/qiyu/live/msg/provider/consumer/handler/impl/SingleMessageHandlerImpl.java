package org.qiyu.live.msg.provider.consumer.handler.impl;

import com.alibaba.fastjson.JSON;
import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.im.dto.ImMsgBody;
import org.qiyu.live.im.router.interfaces.ImRouterRpc;
import org.qiyu.live.living.interfaces.dto.LivingRoomReqDTO;
import org.qiyu.live.living.interfaces.rpc.ILivingRoomRpc;
import org.qiyu.live.msg.dto.MessageDTO;
import org.qiyu.live.im.router.constants.ImMsgBizCodeEum;
import org.qiyu.live.msg.provider.consumer.handler.MessageHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SingleMessageHandlerImpl implements MessageHandler {
    @DubboReference
    private ImRouterRpc routerRpc;

    @DubboReference
    private ILivingRoomRpc livingRoomRpc;

    @Override
    public void onMsgReceive(ImMsgBody imMsgBody) {
        System.out.println("进入onMsgReceive");
        int bizCode = imMsgBody.getBizCode();
        if (bizCode == ImMsgBizCodeEum.LIVING_ROOM_IM_CHAT_MSG_BIZ.getCode()) {
            //一个人发送，n个人接受
            MessageDTO messageDTO = JSON.parseObject(imMsgBody.getData(), MessageDTO.class);
            Integer roomId = messageDTO.getRoomId();
            LivingRoomReqDTO reqDTO = new LivingRoomReqDTO();
            reqDTO.setRoomId(roomId);
            reqDTO.setAppId(imMsgBody.getAppId());


            List<Long> userIdList = livingRoomRpc.queryUserIdsByRoomId(reqDTO).stream().filter(x->!x.equals(imMsgBody.getUserId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(userIdList)) {
                return;
            }
            List<ImMsgBody> imMsgBodies =new ArrayList<>();
            System.out.println("userIdList::"+userIdList);
            userIdList.forEach(userId->{
                ImMsgBody respMsgBody = new ImMsgBody();
                respMsgBody.setUserId(userId);
                respMsgBody.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
                respMsgBody.setBizCode(ImMsgBizCodeEum.LIVING_ROOM_IM_CHAT_MSG_BIZ.getCode());
                respMsgBody.setData(JSON.toJSONString(messageDTO));
                imMsgBodies.add(respMsgBody);
            });
            System.out.println(imMsgBodies);
            //将消息推送给router进行转发给im服务器
            routerRpc.batchSendMsg(imMsgBodies);
        }
    }
}
