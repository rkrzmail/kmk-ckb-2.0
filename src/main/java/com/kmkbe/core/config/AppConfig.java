package com.kmkbe.core.config;

import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class AppConfig {
    private final CustomerRepository customerRepository;

    @Bean
    UserDetailsService userDetailsService() {
        return username -> customerRepository.findByCustEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public BCryptPasswordEncoder bCryptEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextLogoutHandler securityContextLogoutHandler() {
        return new SecurityContextLogoutHandler();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
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
    public RestTemplate provideRestTemplate() {
        return new RestTemplate();
    }

    /*@Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        return mailSender;
    }*/

   /* @Bean
    public AuthService authService() {
        return new AuthService();
    }*/

   /* @Bean
    public UserDetailsService userDetails() {
        UserDetails user = User.builder()
                .username("user")
                .password(bCryptEncoder().encode("123456"))
                .roles("CUSTOMER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }*/
}
