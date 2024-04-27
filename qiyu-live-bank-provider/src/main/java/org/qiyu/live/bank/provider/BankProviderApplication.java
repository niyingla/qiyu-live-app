package org.qiyu.live.bank.provider;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.qiyu.live.bank.constants.PayProductTypeEnum;
import org.qiyu.live.bank.provider.service.IPayProductService;
import org.qiyu.live.bank.provider.service.QiyuCurrencyAccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class BankProviderApplication  implements CommandLineRunner {

    @Resource
    private QiyuCurrencyAccountService qiyuCurrencyAccountService;

    @Resource
    private IPayProductService payProductService;

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(BankProviderApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }


    @Override
    public void run(String... args) throws Exception {
        System.out.println(payProductService.products(PayProductTypeEnum.QIYU_COIN.getCode()));
    }
}
