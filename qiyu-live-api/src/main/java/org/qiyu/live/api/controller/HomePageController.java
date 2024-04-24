package org.qiyu.live.api.controller;



import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.api.service.IHomePageService;
import org.qiyu.live.api.vo.HomePageVO;
import org.qiyu.live.common.interfaces.vo.WebResponseVO;
import org.qiyu.live.user.interfaces.rpc.IUserRpc;
import org.qiyu.live.user.interfaces.rpc.IUserTagRpc;
import org.qiyu.live.web.starter.context.QiyuRequestContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/home")
public class HomePageController {

    @DubboReference
    private IUserRpc userRpc;

    @DubboReference
    private IUserTagRpc userTagRpc;

    @Resource
    private IHomePageService homePageService;


    @PostMapping("/initPage")
    public WebResponseVO initPage() {
        Long userId = QiyuRequestContext.getUserId();
        HomePageVO homePageVO = new HomePageVO();
        homePageVO.setLoginStatus(false);
        if (userId != null) {
            homePageVO = homePageService.initPage(userId);
            homePageVO.setLoginStatus(true);
        }
        return WebResponseVO.success(homePageVO);
    }
}
