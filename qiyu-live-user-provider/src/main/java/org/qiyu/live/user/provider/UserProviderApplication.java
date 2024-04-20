package org.qiyu.live.user.provider;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.qiyu.live.id.generate.interfaces.IdGenerateRpc;
import org.qiyu.live.user.dto.UserLoginDTO;
import org.qiyu.live.user.provider.service.IUserPhoneService;
import org.qiyu.live.user.provider.service.IUserService;
import org.qiyu.live.user.provider.service.IUserTagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户服务中台启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
public class UserProviderApplication  {
    @Resource
    IUserTagService userTagService;
    @Resource
    IUserService userService;
    @Resource
    IUserPhoneService userPhoneService;



    private final static Logger LOGGER = LoggerFactory.getLogger(UserProviderApplication.class);

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(UserProviderApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }

//    @Override
//    public void run(String... args) throws Exception {
//        String phone="18673503116";
//        UserLoginDTO userLoginDTO = userPhoneService.login(phone);
//        System.out.println(userLoginDTO);
//        System.out.println(userPhoneService.queryByUserId(userLoginDTO.getUserId()));
//        System.out.println(userPhoneService.queryByPhone(phone));

//        Long userId=1001L;
//        UserDTO userDTO = userService.getByUserId(userId);
//        userDTO.setNickName("test-nickName");
//        userService.updateUserInfo(userDTO);
//

//        CountDownLatch count = new CountDownLatch(1);
//        for (int i = 0; i < 100; i++) {
//           Thread t1= new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        count.await();
//                        LOGGER.info("result is"+userTagService.setTag(userId,UserTagsEnum.IS_VIP));
//
//                    }catch (InterruptedException e){
//                        throw new RuntimeException(e);
//                    }
//                }
//            });
//           t1.start();
//        }
//        count.countDown();
//        Thread.sleep(100000);

//        userTagService.setTag(userId, UserTagsEnum.IS_VIP);
//        System.out.println("当前用户是否拥有isvip:"+userTagService.containTag(userId,UserTagsEnum.IS_VIP));
//        userTagService.setTag(userId, UserTagsEnum.IS_OLD_USER);
//        System.out.println("当前用户是否拥有isold:"+userTagService.containTag(userId,UserTagsEnum.IS_OLD_USER));
//        userTagService.setTag(userId, UserTagsEnum.IS_OLD_USER);
//        System.out.println("当前用户是否拥有isold:"+userTagService.containTag(userId,UserTagsEnum.IS_OLD_USER));
//        userTagService.setTag(userId, UserTagsEnum.IS_OLD_USER);
//        System.out.println("当前用户是否拥有isold:"+userTagService.containTag(userId,UserTagsEnum.IS_OLD_USER));
//        userTagService.setTag(userId, UserTagsEnum.IS_RICH);
//        System.out.println("当前用户是否拥有isrich:"+userTagService.containTag(userId,UserTagsEnum.IS_RICH));
//        System.out.println("====================");
//        userTagService.cancelTag(userId, UserTagsEnum.IS_VIP);
//        System.out.println("当前用户是否拥有isvip:"+userTagService.containTag(userId,UserTagsEnum.IS_VIP));
//        userTagService.cancelTag(userId, UserTagsEnum.IS_OLD_USER);
//        System.out.println("当前用户是否拥有isold:"+userTagService.containTag(userId,UserTagsEnum.IS_OLD_USER));
//        userTagService.cancelTag(userId, UserTagsEnum.IS_RICH);
//        System.out.println("当前用户是否拥有isrich:"+userTagService.containTag(userId,UserTagsEnum.IS_RICH));
//    }
}