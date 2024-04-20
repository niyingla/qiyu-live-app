package org.qiyu.live.im.router.provider.rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.qiyu.live.im.dto.ImMsgBody;
import org.qiyu.live.im.router.interfaces.ImRouterRpc;
import org.qiyu.live.im.router.provider.service.ImRouterService;


@DubboService
public class ImRouterRpcImpl implements ImRouterRpc {
    @Resource
    private ImRouterService imRouterService;

    @Override
    public boolean sendMsg(ImMsgBody imMsgBody) {
        return imRouterService.sendMsg(imMsgBody);
    }
}
