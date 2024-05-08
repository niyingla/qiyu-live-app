package org.qiyu.live.user.provider.service;

import org.qiyu.live.user.dto.UserDTO;

import java.util.List;
import java.util.Map;

public interface IUserService {
//   根据用户id进行对齐
    UserDTO getByUserId(Long id);

    /**
     * 用户信息更新
     * @param userDTO
     * @return
     */
    boolean updateUserInfo(UserDTO userDTO);

    /**
     * 用户信息插入
     * @param userDTO
     * @return
     */
    boolean insertUserInfo(UserDTO userDTO);

    Map<Long,UserDTO> batchQueryUserInfo(List<Long> userIdList);

}
