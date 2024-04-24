package org.qiyu.live.im.router.provider.service;

import org.qiyu.live.im.dto.ImMsgBody;

import java.util.List;

public interface ImRouterService {
    boolean sendMsg(ImMsgBody imMsgBody);
    /**
     * 直播间内批量发送消息
     * @param imMsgBodyList
     */
    void batchSendMsg(List<ImMsgBody> imMsgBodyList);

}
