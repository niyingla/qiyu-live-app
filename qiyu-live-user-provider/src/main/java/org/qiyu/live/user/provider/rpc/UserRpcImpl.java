package org.qiyu.live.user.provider.rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.qiyu.live.user.dto.UserDTO;
import org.qiyu.live.user.interfaces.rpc.IUserRpc;
import org.qiyu.live.user.provider.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@DubboService
public class UserRpcImpl implements IUserRpc {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRpcImpl.class);

    @Resource
    private IUserService userService;

    @Override
    public UserDTO getByUserId(Long id) {
        System.out.println("UserRpcImpl");
        return userService.getByUserId(id);
    }

    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        return userService.updateUserInfo(userDTO);
    }

    @Override
    public boolean insertUserInfo(UserDTO userDTO) {
        return userService.insertUserInfo(userDTO);
    }

    @Override
    public Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdList) {
        return userService.batchQueryUserInfo(userIdList);
    }
}