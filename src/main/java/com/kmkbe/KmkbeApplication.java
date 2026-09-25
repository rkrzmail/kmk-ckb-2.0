package com.kmkbe;


import com.kmkbe.config.RsaKeyConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@SpringBootApplication
@EnableConfigurationProperties(RsaKeyConfigProperties.class)
@EnableWebMvc
@EnableAsync
@EnableAspectJAutoProxy
@EnableFeignClients
@EnableScheduling
@EnableCaching
public class KmkbeApplication {

    public static void main(String[] args) {
        SpringApplication.run(KmkbeApplication.class, args);
    }
}
