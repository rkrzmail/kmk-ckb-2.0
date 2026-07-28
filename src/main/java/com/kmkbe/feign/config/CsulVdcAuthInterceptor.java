package com.kmkbe.feign.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class CsulVdcAuthInterceptor implements RequestInterceptor {

  private final CsulTokenManager csulTokenManager;

  public CsulVdcAuthInterceptor(CsulTokenManager csulTokenManager) {
    this.csulTokenManager = csulTokenManager;
  }

  @Override
  public void apply(RequestTemplate template) {
    String url = template.url();

    // Robust condition check covering relative paths and complete targets
    if (url.contains("/webhook/token") || url.endsWith("/token") || template.feignTarget().name().equals("authClient")) {
      return; // Skip token injection completely for login calls
    }

    template.header("Authorization", csulTokenManager.getToken());
  }
}
