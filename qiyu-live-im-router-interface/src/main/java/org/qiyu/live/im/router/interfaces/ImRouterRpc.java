package org.qiyu.live.im.router.interfaces;

import org.qiyu.live.im.dto.ImMsgBody;

import java.util.List;

public interface ImRouterRpc {

    /**
     * 按照用户id进行消息的发送
     */
    boolean sendMsg(ImMsgBody imMsgBody);

    /**
     * 直播间内批量发送消息
     * @param imMsgBodyList
     */
    void batchSendMsg(List<ImMsgBody> imMsgBodyList);
}