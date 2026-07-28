package com.kmkbe.feign.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfinsR3AuthInterceptor implements RequestInterceptor {

  @Value("${feign.confins.adins-key}")
  private String adinsKey;

  @Override
  public void apply(RequestTemplate template) {
    template.header("Adinskey", adinsKey);
  }
}
