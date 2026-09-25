package com.kmkbe.feign.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.RedirectStrategy;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.Arrays;

/**
 * @author hyvercode
 * @date 8/12/26
 */


@Configuration
public class FeignHttpClientConfig {

  @Bean
  public CloseableHttpClient httpClient() {
    return HttpClients.custom()
      .setRedirectStrategy(new RedirectStrategy() {
        @Override
        public boolean isRedirected(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) {
          int status = httpResponse.getCode();
          return Arrays.asList(301, 302, 303, 307).contains(status);
        }

        @Override
        public URI getLocationURI(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) {
          return null;
        }
      })
      .build();
  }
}

