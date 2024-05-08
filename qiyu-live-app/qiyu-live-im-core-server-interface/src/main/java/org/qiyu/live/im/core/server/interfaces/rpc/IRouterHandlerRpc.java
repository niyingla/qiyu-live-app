package org.qiyu.live.im.core.server.interfaces.rpc;

import org.qiyu.live.im.dto.ImMsgBody;

import java.util.List;

public interface IRouterHandlerRpc {

    /**
     * 按照用户id进行消息的发送
     */
    void sendMsg(ImMsgBody imMsgBody);

    /**
     * 直播间内批量发送消息
     * @param imMsgBody
     */
    void batchSendMsg(List<ImMsgBody> imMsgBody);
}