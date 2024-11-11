package com.kmkbe;


import com.kmkbe.core.config.RsaKeyConfigProperties;
import com.kmkbe.core.utils.ExceptionUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

@SpringBootApplication
@EnableConfigurationProperties(RsaKeyConfigProperties.class)
@EnableWebMvc
@EnableAsync
@EnableAspectJAutoProxy
public class KmkbeApplication {

    public static void main(String[] args) {
       /* HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });*/
        SpringApplication.run(KmkbeApplication.class, args);
        /*String s = "{\"includeCount\":true,\"includeData\":true,\"isLoading\":true,\"queryString\":{\"name\":\"lookupZipcode\"},\"rowVersion\":\"\",\"joinType\":\"INNER\",\"pageNo\":2,\"rowPerPage\":10,\"criteria\":[{\"high\":0,\"isCriteriaDataTable\":false,\"low\":0,\"propName\":\"RZ.CITY\",\"restriction\":\"Like\",\"value\":\"%JAKARTA%\",\"DataType\":\"\"}],\"RequestDateTime\":\"2024-11-02\"}";
        System.out.println(s);*/

    }
}
