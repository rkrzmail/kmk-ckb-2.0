package com.kmkbe.feign.config;

import com.kmkbe.feign.model.request.PostLoginRequest;
import com.kmkbe.feign.model.dto.PostLoginDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class CsulTokenManager {

  @Value("${feign.csul.ckb.url}")
  private String apiBaseCKB;

  private final RestTemplate restTemplate = new RestTemplate();
  private final AtomicReference<String> cachedToken = new AtomicReference<>();

  public String getToken() {
    if (cachedToken.get() == null) {
      synchronized (this) {
        if (cachedToken.get() == null) {
          executeLogin();
        }
      }
    }
    return cachedToken.get();
  }

  public void clearTokenAndRelogin() {
    synchronized (this) {
      cachedToken.set(null);
      executeLogin();
    }
  }

  private void executeLogin() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    PostLoginRequest credentials = new PostLoginRequest("ramco", "RamcoVDC2026!", "SecretRamco2026!");
    HttpEntity<PostLoginRequest> requestEntity = new HttpEntity<>(credentials, headers);

    try {
      ResponseEntity<PostLoginDto> responseEntity = restTemplate.postForEntity(apiBaseCKB.concat("/api/v1/webhook/token"), requestEntity, PostLoginDto.class);
      PostLoginDto response = responseEntity.getBody();
      log.info("RAMCO {} ",response);
      if (response != null && response.getData().getToken()!= null) {
        cachedToken.set("Bearer " + response.getData().getToken());
      } else {
        throw new RuntimeException("Authentication failed: Token string payload was null");
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to fetch auth token via standard RestTemplate", e);
    }
  }
}
