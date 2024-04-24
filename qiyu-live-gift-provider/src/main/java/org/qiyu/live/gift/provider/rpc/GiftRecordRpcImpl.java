package org.qiyu.live.gift.provider.rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.qiyu.live.gift.dto.GiftRecordDTO;
import org.qiyu.live.gift.provider.service.IGiftRecordService;
import org.qiyu.live.gift.rpc.IGiftRecordRpc;
import org.springframework.stereotype.Service;

@DubboService
public class GiftRecordRpcImpl implements IGiftRecordRpc {
    @Resource
    IGiftRecordService giftRecordService;
    @Override
    public void insertOne(GiftRecordDTO giftRecordDTO) {
        giftRecordService.insertOne(giftRecordDTO);
    }
}
