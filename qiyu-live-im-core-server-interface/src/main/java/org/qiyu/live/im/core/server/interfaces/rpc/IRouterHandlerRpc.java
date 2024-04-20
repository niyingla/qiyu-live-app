package org.qiyu.live.im.core.server.interfaces.rpc;

import org.qiyu.live.im.dto.ImMsgBody;

public interface IRouterHandlerRpc {

    /**
     * 按照用户id进行消息的发送
     */
    void sendMsg(ImMsgBody imMsgBody);
}