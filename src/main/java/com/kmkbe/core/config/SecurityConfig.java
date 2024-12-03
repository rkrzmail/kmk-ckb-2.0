package com.kmkbe.core.config;

import com.kmkbe.core.callback.UnauthorizedEntryPoint;
import com.kmkbe.core.middleware.ApiKeyAuthenticationFilter;
import com.kmkbe.core.middleware.JwtAuthenticationFilter;
import com.kmkbe.core.service.ApiKeyAuthenticationService;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${spring.profiles.active}")
    private String profileActives;

    @Value("${security.api.key}")
    private String apiKey;

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RsaKeyConfigProperties rsaKeyConfigProperties;
    private final UserDetailsService userDetailsService;
    private final UnauthorizedEntryPoint unauthorizedEntryPoint;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    public SecurityConfig(
            AuthenticationProvider authenticationProvider,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RsaKeyConfigProperties rsaKeyConfigProperties,
            UserDetailsService userDetailsService,
            UnauthorizedEntryPoint unauthorizedEntryPoint
    ) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rsaKeyConfigProperties = rsaKeyConfigProperties;
        this.userDetailsService = userDetailsService;
        this.unauthorizedEntryPoint = unauthorizedEntryPoint;
        this.apiKeyAuthenticationFilter = new ApiKeyAuthenticationFilter("/api/v1");
        //this.apiKeyAuthenticationFilter = new ApiKeyAuthenticationFilter();
    }


    @Bean
    public FilterRegistrationBean<CustomFilter> customFilter() {
        FilterRegistrationBean<CustomFilter> registrationBean = new FilterRegistrationBean<CustomFilter>();
        registrationBean.setFilter(new CustomFilter());
        registrationBean.addUrlPatterns("/api/*"); // Hanya berlaku untuk endpoint tertentu
        registrationBean.setOrder(1); // Prioritas filter
        return registrationBean;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        /*apiKeyAuthenticationFilter.setRequiresAuthenticationRequestMatcher(new OrRequestMatcher(
                new AntPathRequestMatcher("/api/v1/financing/invoice-paid")
                //new AntPathRequestMatcher("/api/secured/**")
        ));*/

        apiKeyAuthenticationFilter.setAuthenticationManager(authentication -> {
            String providedApiKey = (String) authentication.getPrincipal();
            if (providedApiKey.equals(apiKey)) {
                return new ApiKeyAuthenticationService(
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                        apiKey
                );
            } else {
                authentication.setAuthenticated(false);
            }

            return authentication;
        });

        http
                .csrf(AbstractHttpConfigurer::disable)
                //.cors(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(e -> {
                            e.authenticationEntryPoint(unauthorizedEntryPoint);
                        }
                )
                .authorizeHttpRequests(auth -> {
                            auth.requestMatchers(JwtAuthenticationFilter.ENDPOINTS_WHITELIST).permitAll();

                            //determine dev env should has Swaggers access freely
                            if (profileActives.equals("dev")) {
                                auth.requestMatchers(JwtAuthenticationFilter.ENDPOINTS_SWAGGERS).permitAll();
                            }

                            auth.requestMatchers(JwtAuthenticationFilter.ENDPOINTS_WHITELIST_LOAN_SUBMISSION).permitAll();
                            auth.requestMatchers(JwtAuthenticationFilter.ENDPOINTS_WHITELIST_FINANCING).permitAll();

                            auth.dispatcherTypeMatchers(
                                    DispatcherType.ASYNC,
                                    DispatcherType.FORWARD,
                                    DispatcherType.ERROR
                            ).permitAll();
                        }
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .userDetailsService(userDetailsService);

        //.oauth2ResourceServer((oauth2) -> oauth2.jwt((jwt) -> jwt.decoder(jwtDecoder())))
        //.httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        corsConfiguration.addAllowedOriginPattern("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(rsaKeyConfigProperties.publicKey()).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(rsaKeyConfigProperties.publicKey()).privateKey(rsaKeyConfigProperties.privateKey()).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    SecurityContextLogoutHandler logoutHandler() {
        return new SecurityContextLogoutHandler();
    }
}
