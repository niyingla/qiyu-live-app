package org.qiyu.live.im.provider;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.im.provider.service.ImOnlineService;
import org.qiyu.live.im.provider.service.ImTokenService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class ImProviderApplication implements CommandLineRunner {
    @Resource
    private ImOnlineService onlineService;
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(ImProviderApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        for(int i = 0; i < 10; i++) {
            System.out.println(onlineService.isOnline(1001L + i, AppIdEnum.QIYU_LIVE_BIZ.getCode()));
        }
    }
}
