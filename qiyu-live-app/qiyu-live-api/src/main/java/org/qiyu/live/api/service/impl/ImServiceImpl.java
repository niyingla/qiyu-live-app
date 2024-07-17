package org.qiyu.live.api.service.impl;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.api.service.ImService;
import org.qiyu.live.api.vo.ImConfigVO;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.im.interfaces.ImTokenRpc;
import org.qiyu.live.web.starter.context.QiyuRequestContext;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ImServiceImpl implements ImService {
    @DubboReference
    private ImTokenRpc imTokenRpc;

    @Resource
    private DiscoveryClient discoveryClient;

    @Override
    public ImConfigVO getImConfig() {
        ImConfigVO imConfigVO = new ImConfigVO();
        String token = imTokenRpc.createImLoginToken(QiyuRequestContext.getUserId(), AppIdEnum.QIYU_LIVE_BIZ.getCode());
        imConfigVO.setToken(token);
        getImServerAddress(imConfigVO);
        return imConfigVO;
    }

    private void getImServerAddress(ImConfigVO imConfigVO){
        //获取当前服务的所有实例
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances("qiyu-live-im-core-server");
        //乱序
        Collections.shuffle(serviceInstances);
        ServiceInstance instance = serviceInstances.get(0);
        //设置im服务地址
        imConfigVO.setWsImServerAddress(instance.getHost() + ":8086");
        imConfigVO.setTcpImServerAddress(instance.getHost() + ":8085");

    }
}
