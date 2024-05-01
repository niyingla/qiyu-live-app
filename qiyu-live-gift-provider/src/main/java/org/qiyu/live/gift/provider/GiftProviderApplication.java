package org.qiyu.live.gift.provider;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.qiyu.live.gift.rpc.ISkuStockInfoRpc;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
public class GiftProviderApplication  {
    @Resource
    private ISkuStockInfoRpc skuStockInfoRpc;

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(GiftProviderApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }

//    @Override
//    public void run(String... args) throws Exception {
//        Long anchorId=10852L;
//        Long skuId=90713L;
//        skuStockInfoRpc.prepareStockInfo(anchorId);
//        for (int i = 0; i < 11; i++) {
//            boolean b = skuStockInfoRpc.decrStockNumBySkuIdByLua(skuId, 10);
//            System.out.println(b);
//        }
//    }
}
