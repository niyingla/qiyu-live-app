package org.qiyu.live.msg.provider;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.qiyu.live.msg.dto.MsgCheckDTO;
import org.qiyu.live.msg.enums.MsgSendResultEnum;
import org.qiyu.live.msg.provider.service.ISmsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.Scanner;

@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
public class MsgProviderApplication  {
//    @Resource
//    private ISmsService smsService;
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(MsgProviderApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }

//    @Override
//    public void run(String... args) throws Exception {
//        String phone="19891933422";
//        MsgSendResultEnum message = smsService.sendMessage(phone);
//        System.out.println(message );
//        while (true){
//            System.out.println("输入验证码：");
//            Scanner scanner = new Scanner(System.in);
//            int i = scanner.nextInt();
//            MsgCheckDTO checkDTO = smsService.checkLoginCode(phone, i);
//            System.out.println(checkDTO);
//        }
//    }
}

