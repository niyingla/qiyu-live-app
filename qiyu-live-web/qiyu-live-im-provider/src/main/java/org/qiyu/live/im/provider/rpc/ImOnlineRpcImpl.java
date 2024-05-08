package org.qiyu.live.im.provider.rpc;

import org.apache.dubbo.config.annotation.DubboService;
import org.qiyu.live.im.interfaces.ImOnlineRpc;

@DubboService
public class ImOnlineRpcImpl implements ImOnlineRpc {
    @Override
    public boolean isOnline(Long userId, int appId) {
        return false;
    }
}
