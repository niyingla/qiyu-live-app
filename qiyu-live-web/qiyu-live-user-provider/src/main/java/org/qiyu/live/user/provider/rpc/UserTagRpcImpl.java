package org.qiyu.live.user.provider.rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.qiyu.live.user.constants.UserTagsEnum;
import org.qiyu.live.user.interfaces.rpc.IUserTagRpc;
import org.qiyu.live.user.provider.service.IUserTagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DubboService
public class UserTagRpcImpl implements IUserTagRpc {
    private static final Logger LOGGER = LoggerFactory.getLogger(IUserTagRpc.class);



    @Resource
    IUserTagService userTagService;

    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagsEnum) {
        return userTagService.setTag(userId,userTagsEnum);
    }

    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagsEnum) {
        return userTagService.cancelTag(userId,userTagsEnum);
    }

    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        return userTagService.containTag(userId,userTagsEnum);
    }
}
