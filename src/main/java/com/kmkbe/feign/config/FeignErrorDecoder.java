package com.kmkbe.feign.config;

import com.kmkbe.feign.exception.VendorNotFoundException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

  private final CsulTokenManager csulTokenManager;
  private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

  public FeignErrorDecoder(CsulTokenManager csulTokenManager) {
    this.csulTokenManager = csulTokenManager;
  }

  @Override
  public Exception decode(String methodKey, Response response) {
    // HTTP 401 means the stored token is expired or dead
    if (response.status() == 401) {
      try {
        // Clear state, trigger re-login right away
        csulTokenManager.clearTokenAndRelogin();
      } catch (Exception e) {
        return new RuntimeException("Failed to re-authenticate after token expiration", e);
      }
    }

    // 2. Handle embedded JSON errors (like HTTP 404 or custom JSON response body)
    if (response.body() != null) {
      try {
        // Safely read body without breaking the stream for downstream
        String bodyString = Util.toString(response.body().asReader(StandardCharsets.UTF_8));

        // Reconstruct response so defaultDecoder can still read it if needed
        response = response.toBuilder().body(bodyString, StandardCharsets.UTF_8).build();

        // Quick string containment check for efficiency
        if (bodyString.contains("\"status\":\"FAILED\"") || bodyString.contains("\"statusCode\":404")) {
          // Extract message dynamically if needed, or throw custom domain exception
          return new VendorNotFoundException("Vendor tidak ditemukan");
        }
      } catch (IOException e) {
        // Fallback if body parsing fails
      }
    }

    return defaultDecoder.decode(methodKey, response);
  }
}
