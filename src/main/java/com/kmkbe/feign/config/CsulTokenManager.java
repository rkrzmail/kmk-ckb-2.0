package com.kmkbe.feign.config;

import com.kmkbe.exception.BusinessException;
import com.kmkbe.feign.model.request.CsulPostLoginRequest;
import com.kmkbe.feign.model.dto.CsulPostLoginDto;
import com.kmkbe.helpers.constant.ErrorConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class CsulTokenManager {

  @Value("${feign.csul.ckb.url}")
  private String apiBaseCKB;

  @Value("${feign.csul.ckb.username}")
  private String username;

  @Value("${feign.csul.ckb.password}")
  private String password;

  @Value("${feign.csul.ckb.client-secret}")
  private String clientSecret;

  private final RestTemplate restTemplate;
  private final AtomicReference<String> cachedToken = new AtomicReference<>();

  public CsulTokenManager(@Qualifier("restTemplate") RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public String getToken() {
      synchronized (this) {
          executeLogin();
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
    CsulPostLoginRequest credentials = new CsulPostLoginRequest(username, password, clientSecret);
    HttpEntity<CsulPostLoginRequest> requestEntity = new HttpEntity<>(credentials, headers);

    try {
      String fullUrl = UriComponentsBuilder.fromHttpUrl(apiBaseCKB)
        .path("/api/v1/webhook/token")
        .toUriString();

      ResponseEntity<CsulPostLoginDto> responseEntity = restTemplate.postForEntity(fullUrl, requestEntity, CsulPostLoginDto.class);
      CsulPostLoginDto response = responseEntity.getBody();

      log.info("CKB {} ", response);

      if (response != null && response.getData() != null && response.getData().getToken() != null) {
        cachedToken.set("Bearer " + response.getData().getToken());
      } else {
        log.info("{} {}", ErrorConstant.ERROR_MESSAGE_80, response);
        throw new BusinessException(
          HttpStatus.CONFLICT,
          ErrorConstant.ERROR_CODE_80,
          "Authentication failed: Token string payload was null or response data was empty"
        );
      }
    } catch (BusinessException be) {
      throw be;
    } catch (Exception e) {
      log.error("Login failed due to system error: {} ", e.getMessage());
      throw new BusinessException(
        HttpStatus.CONFLICT,
        ErrorConstant.ERROR_CODE_80,
        "Authentication failed response data was empty"
      );
    }
  }
}
