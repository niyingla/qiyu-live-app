package org.qiyu.live.api.controller;

import jakarta.annotation.Resource;
import org.qiyu.live.api.service.ImService;
import org.qiyu.live.api.vo.ImConfigVO;
import org.qiyu.live.common.interfaces.vo.WebResponseVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/im")
public class ImController {

    @Resource
    private ImService imService;

    @PostMapping("/getImConfig")
    public WebResponseVO getImConfig() {
        ImConfigVO imConfigVO = imService.getImConfig();
        System.out.println(imConfigVO);
        return WebResponseVO.success(imConfigVO);
    }
}
