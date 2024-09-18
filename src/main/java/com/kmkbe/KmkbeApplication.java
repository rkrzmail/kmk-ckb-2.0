package com.kmkbe;


import com.kmkbe.core.config.RsaKeyConfigProperties;
import com.kmkbe.core.utils.ExceptionUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableConfigurationProperties(RsaKeyConfigProperties.class)
@EnableWebMvc
@EnableAsync
@EnableAspectJAutoProxy
public class KmkbeApplication {

    public static void main(String[] args) {
        SpringApplication.run(KmkbeApplication.class, args);
    }
}
