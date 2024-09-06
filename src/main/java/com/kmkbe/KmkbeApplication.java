package com.kmkbe;


import com.kmkbe.core.config.RsaKeyConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(RsaKeyConfigProperties.class)
@SpringBootApplication
public class KmkbeApplication {

    public static void main(String[] args) {
        SpringApplication.run(KmkbeApplication.class, args);
    }
}
