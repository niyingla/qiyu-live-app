package org.qiyu.live.id.generate.provider;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.qiyu.live.id.generate.provider.service.IdGenerateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class IdGenerateApplication implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdGenerateApplication.class);

    @Resource
    private IdGenerateService idGenerateService;

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(IdGenerateApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }


    @Override
    public void run(String... args) throws Exception {
        for (int i = 0; i < 50; i++) {
            Long id = idGenerateService.getSeqId(1);
            System.out.println(id);
        }
    }
}
