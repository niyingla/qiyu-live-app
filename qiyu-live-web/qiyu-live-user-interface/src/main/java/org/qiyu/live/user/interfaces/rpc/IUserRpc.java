package org.qiyu.live.user.interfaces.rpc;

import org.qiyu.live.user.dto.UserDTO;

import java.util.List;
import java.util.Map;


public interface IUserRpc {

    /**
     *
     * @param id
     * @return
     */

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
