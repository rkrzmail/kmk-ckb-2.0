package com.kmkbe.config;

import com.kmkbe.core.callback.UnauthorizedEntryPoint;
import com.kmkbe.core.middleware.ApiKeyAuthenticationFilter;
import com.kmkbe.core.middleware.JwtAuthenticationFilter;
import com.kmkbe.core.service.ApiKeyAuthenticationService;
import com.kmkbe.core.utils.SecurityUtils;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Value("${spring.profiles.active:dev}")
  private String profileActives;

  @Value("${security.api.key}")
  private String apiKey;

  @Value("${cors.allowed-origins:https://dev1-danasakti.csulfinance.com,https://danasakti.csulfinance.com}")
  private String corsAllowedOrigins;

  @Value("${cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
  private String corsAllowedMethods;

  @Value("${cors.allowed-headers:*}")
  private String corsAllowedHeaders;

  @Value("${cors.exposed-headers:Referrer-Policy,Authorization,rtoken,ApiKey}")
  private String corsExposedHeaders;

  @Value("${cors.allow-credentials:true}")
  private boolean corsAllowCredentials;

  @Value("${cors.max-age:3600}")
  private Long corsMaxAge;

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
    @Qualifier("userDetailsService")
    UserDetailsService userDetailsService,
    UnauthorizedEntryPoint unauthorizedEntryPoint
  ) {
    this.authenticationProvider = authenticationProvider;
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.rsaKeyConfigProperties = rsaKeyConfigProperties;
    this.userDetailsService = userDetailsService;
    this.unauthorizedEntryPoint = unauthorizedEntryPoint;
    this.apiKeyAuthenticationFilter = new ApiKeyAuthenticationFilter("/api/v1");
  }


  /**
   * Mengamankan webSecurityCustomizer agar tidak lagi meng-ignore request secara luas (Issue C7).
   * Hanya file statis benar-benar publik yang di-ignore; seluruh endpoint API, auth, swagger, dan
   * dokumen wajib melewati SecurityFilterChain agar filter keamanan & otorisasi tetap aktif.
   */
  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers(
      "/static/**"
    );
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
    apiKeyAuthenticationFilter.setAuthenticationManager(authentication -> {
      String providedApiKey = (String) authentication.getPrincipal();
      if (SecurityUtils.constantTimeEquals(providedApiKey, apiKey)) {
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
          // Preflight CORS request selalu permitAll agar browser options tidak diblokir security
          auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
          auth.requestMatchers("/api/v1/digital-sign/callback").permitAll();
          auth.requestMatchers(JwtAuthenticationFilter.ENDPOINTS_WHITELIST).permitAll();

          // Swagger/OpenAPI hanya dibuka secara bebas untuk profile dev/local (Issue H3)
          if (isSwaggerAllowedProfile()) {
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

    // Parse and set explicit allowed origins (Issue C8: prevent wildcard * with allowCredentials=true)
    List<String> origins = parseCommaSeparatedList(corsAllowedOrigins);
    if (origins.contains("*")) {
      // If wildcard is configured, allowCredentials must NEVER be true per CORS specifications
      corsConfiguration.addAllowedOriginPattern("*");
      corsConfiguration.setAllowCredentials(false);
    } else {
      for (String origin : origins) {
        if (origin.contains("*")) {
          corsConfiguration.addAllowedOriginPattern(origin);
        } else {
          corsConfiguration.addAllowedOrigin(origin);
        }
      }
      corsConfiguration.setAllowCredentials(corsAllowCredentials);
    }

    corsConfiguration.setAllowedMethods(parseCommaSeparatedList(corsAllowedMethods));
    corsConfiguration.setAllowedHeaders(parseCommaSeparatedList(corsAllowedHeaders));

    List<String> exposedHeaders = parseCommaSeparatedList(corsExposedHeaders);
    if (!exposedHeaders.isEmpty()) {
      corsConfiguration.setExposedHeaders(exposedHeaders);
    }

    if (corsMaxAge != null) {
      corsConfiguration.setMaxAge(corsMaxAge);
    }

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfiguration);
    return source;
  }

  private List<String> parseCommaSeparatedList(String value) {
    if (value == null || value.trim().isEmpty()) {
      return Collections.emptyList();
    }
    return java.util.Arrays.stream(value.split(","))
      .map(String::trim)
      .filter(s -> !s.isEmpty())
      .toList();
  }

  private boolean isSwaggerAllowedProfile() {
    if (profileActives == null) {
      return false;
    }
    String normalized = profileActives.trim().toLowerCase();
    return normalized.equals("dev") || normalized.equals("local")
      || normalized.contains("dev") || normalized.contains("local");
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

//  @Bean
//  @RequestScope(proxyMode = ScopedProxyMode.INTERFACES)
//  public Authentication currentAuthentication() {
//    return SecurityContextHolder.getContext().getAuthentication();
//  }

  @Bean
  SecurityContextLogoutHandler logoutHandler() {
    return new SecurityContextLogoutHandler();
  }
}
