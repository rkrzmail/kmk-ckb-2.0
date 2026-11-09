package com.kmkbe.config;

import feign.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
  /**
   * Level FULL mencatat seluruh header & body request/response ke log,
   * termasuk kredensial ke sistem pihak ketiga (Confins/SISCA).
   * Default BASIC; FULL hanya untuk troubleshooting non-prod via env var.
   */
  @Value("${feign.logger-level:BASIC}")
  private String loggerLevel;

  @Bean
  Logger.Level feignLoggerLevel() {
    return Logger.Level.valueOf(loggerLevel.toUpperCase());
  }
}
