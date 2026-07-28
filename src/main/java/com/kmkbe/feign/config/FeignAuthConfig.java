package com.kmkbe.feign.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignAuthConfig {

  private final CsulTokenManager csulTokenManager;

  public FeignAuthConfig(CsulTokenManager csulTokenManager) {
    this.csulTokenManager = csulTokenManager;
  }

  // Automatically injects the stored token into header
  @Bean
  public RequestInterceptor requestInterceptor() {
    return requestTemplate -> {
      // Skip injecting token if the request is destined for the auth endpoint itself
      if (!requestTemplate.url().contains("/webhook/token")) {
        requestTemplate.header("Authorization", csulTokenManager.getToken());
      }
    };
  }
}
