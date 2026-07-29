package com.kmkbe.config;

import com.kmkbe.core.factory.CustomClientHttpRequestFactory;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.user.repository.MstUserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.TimeZone;

@Configuration
@RequiredArgsConstructor
public class AppConfig {
    @Lazy
    private final CustomerRepository customerRepository;

    @Lazy
    private final MstUserRepository internalUserRepository;


    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jakarta"));
    }

   /* @Bean
    @Lazy
    public RestTemplate restTemplate(ObjectMapper objectMapper) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new ObjectToUrlEncodedConverter(objectMapper));
        return restTemplate;
    }*/

    @Bean(name = "restTemplateByPassSSL")
    @Lazy
    public RestTemplate restTemplateByPassSSL() {
        return new RestTemplate(new CustomClientHttpRequestFactory());
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> customerRepository.findByCustEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean("internalUserDetailService")
    UserDetailsService internalUserDetailsService() {
        return username -> internalUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public AuthenticationManager authenticationManager(@Lazy AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(bCryptEncoder());
        return provider;
    }

    @Bean
    public BCryptPasswordEncoder bCryptEncoder() {
        return new BCryptPasswordEncoder();
    }
}
