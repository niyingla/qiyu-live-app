package org.qiyu.live.msg.provider.rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.qiyu.live.msg.dto.MsgCheckDTO;
import org.qiyu.live.msg.enums.MsgSendResultEnum;
import org.qiyu.live.msg.interfaces.ISmsRpc;
import org.qiyu.live.msg.provider.service.ISmsService;
@DubboService
public class SmsRpcImpl implements ISmsRpc {
    @Resource
    ISmsService iSmsService;

    @Override
    public MsgSendResultEnum sendLoginCode(String phone) {
        return iSmsService.sendMessage(phone);
    }

    @Override
    public MsgCheckDTO checkLoginCode(String phone, Integer code) {
        return iSmsService.checkLoginCode(phone, code);
    }


}
