package org.qiyu.live.api.controller;

import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.user.dto.UserDTO;
import org.qiyu.live.user.interfaces.rpc.IUserRpc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @DubboReference
    private IUserRpc userRpc;

    @GetMapping(value = "/getUsesrInfo")
    public UserDTO getUsesrInfo(Long id){
        UserDTO userDTO=userRpc.getByUserId(id);
        return userDTO;
    }

    @GetMapping(value = "/updateUserInfo")
    public boolean getUserInfo(Long id,String nickName){
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(id);
        userDTO.setNickName(nickName);
        return userRpc.updateUserInfo(userDTO);
    }


    @GetMapping(value = "/insertOne")
    public boolean insertUserInfo(Long id){
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(id);
        userDTO.setNickName("hlyiiorc");
        userDTO.setSex(1);
        return userRpc.insertUserInfo(userDTO);
    }

    @GetMapping("/batchQueryUserInfo")
    public Map<Long,UserDTO> batchQueryUserInfo(String userIdStr) {
        String[] idStr = userIdStr.split(",");
        List<Long> userIdList = new ArrayList<>();
        for (String userId : idStr) {
            userIdList.add(Long.valueOf(userId));
        }
        return userRpc.batchQueryUserInfo(userIdList);
    }
}