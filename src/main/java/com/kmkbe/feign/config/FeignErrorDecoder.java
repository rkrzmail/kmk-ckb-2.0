package com.kmkbe.feign.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

  private final TokenManager tokenManager;
  private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

  public FeignErrorDecoder(TokenManager tokenManager) {
    this.tokenManager = tokenManager;
  }

  @Override
  public Exception decode(String methodKey, Response response) {
    // HTTP 401 means the stored token is expired or dead
    if (response.status() == 401) {
      try {
        // Clear state, trigger re-login right away
        tokenManager.clearTokenAndRelogin();
      } catch (Exception e) {
        return new RuntimeException("Failed to re-authenticate after token expiration", e);
      }
    }
    return defaultDecoder.decode(methodKey, response);
  }
}
