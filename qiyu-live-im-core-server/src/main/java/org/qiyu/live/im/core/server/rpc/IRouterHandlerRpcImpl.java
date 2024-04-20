package org.qiyu.live.im.core.server.rpc;

import org.apache.dubbo.config.annotation.DubboService;
import org.qiyu.live.im.core.server.interfaces.rpc.IRouterHandlerRpc;
import org.qiyu.live.im.dto.ImMsgBody;

@DubboService
public class IRouterHandlerRpcImpl implements IRouterHandlerRpc {
    @Override
    public void sendMsg(ImMsgBody imMsgBody) {
        System.out.println("this is im-core-server");
    }
}
