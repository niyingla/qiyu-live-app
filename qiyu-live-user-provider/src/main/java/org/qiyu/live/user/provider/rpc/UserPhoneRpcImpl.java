package org.qiyu.live.user.provider.rpc;


import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import org.qiyu.live.user.dto.UserLoginDTO;
import org.qiyu.live.user.dto.UserPhoneDTO;
import org.qiyu.live.user.interfaces.rpc.IUserPhoneRPC;
import org.qiyu.live.user.provider.service.IUserPhoneService;

import java.util.List;


@DubboService
public class UserPhoneRpcImpl implements IUserPhoneRPC {
    @Resource
    IUserPhoneService iUserPhoneService;
    @Override
    public UserLoginDTO login(String phone) {
        return iUserPhoneService.login(phone);
    }

    @Override
    public UserPhoneDTO queryByPhone(String phone) {
        return iUserPhoneService.queryByPhone(phone);
    }

    @Override
    public List<UserPhoneDTO> queryByUserId(Long userId) {
        return iUserPhoneService.queryByUserId(userId);
    }

}
