package com.kmkbe.core.utils;

import com.kmkbe.core.domain.model.CommonResult;
import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionUtilsFeignTest {

  @Test
  void handleException_withRetryableException_returnsHumanReadableMessage() {
    Request request = Request.create(Request.HttpMethod.POST, "https://uat-confinsr3.csulfinance.com/api/fou/v1/Generic/GetPagingObjectBySQL",
      Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
    RetryableException retryable = new RetryableException(
      504,
      "Connect timed out executing POST https://uat-confinsr3.csulfinance.com/api/fou/v1/Generic/GetPagingObjectBySQL",
      Request.HttpMethod.POST,
      new SocketTimeoutException("Connect timed out"),
      1000L,
      request
    );

    ResponseEntity<CommonResult<Object>> response = ExceptionUtils.handleException(retryable, null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isFalse();
    assertThat(response.getBody().getCode()).isEqualTo(504);
    assertThat(response.getBody().getMessage())
      .isEqualTo("Koneksi ke layanan eksternal (Confins) sedang mengalami gangguan atau timeout. Silakan coba kembali beberapa saat lagi.");
    assertThat(response.getBody().getMessage()).doesNotContain("https://");
    assertThat(response.getBody().getData()).isNotNull();
  }

  @Test
  void handleException_withWrappedTimeoutException_returnsHumanReadableMessage() {
    RuntimeException wrapped = new RuntimeException("Failed to call Confins API: Connect timed out executing POST https://uat-confinsr3.csulfinance.com");

    ResponseEntity<CommonResult<Object>> response = ExceptionUtils.handleException(wrapped, null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage())
      .isEqualTo("Koneksi ke layanan eksternal (Confins) sedang mengalami gangguan atau timeout. Silakan coba kembali beberapa saat lagi.");
  }

  @Test
  void handleException_withFeignException_returnsHumanReadableMessage() {
    Request request = Request.create(Request.HttpMethod.POST, "https://uat-confinsr3.csulfinance.com/api/fou/v1/Generic/GetPagingObjectBySQL",
      Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
    FeignException feignException = new FeignException.InternalServerError(
      "Internal Server Error",
      request,
      null,
      null
    );

    ResponseEntity<CommonResult<Object>> response = ExceptionUtils.handleException(feignException, null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage())
      .isEqualTo("Terjadi kendala komunikasi dengan layanan eksternal. Silakan coba kembali beberapa saat lagi.");
  }
}
