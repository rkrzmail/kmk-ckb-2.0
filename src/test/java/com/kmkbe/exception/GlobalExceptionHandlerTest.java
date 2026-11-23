package com.kmkbe.exception;

import com.kmkbe.core.exception.LoanDocMandatoryException;
import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {
  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void testHandleLoanDocMandatoryException() {
    var response = handler.handleLoanDocMandatoryException(new LoanDocMandatoryException("Missing mandatory document XYZ."));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isFalse();
    assertThat(response.getBody().getCode()).isEqualTo(400);
    assertThat(response.getBody().getMessage()).isEqualTo("Missing mandatory document XYZ.");
  }

  @Test
  void testHandleBusinessException() {
    var response = handler.handleBusinessException(new BusinessException(HttpStatus.FORBIDDEN, 5001, "Access Denied"));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("Business Error");
    assertThat(response.getBody().getCode()).isEqualTo(5001);
    assertThat(response.getBody().getMessage()).isEqualTo("Access Denied");
  }

  @Test
  void testHandleMethodArgumentNotValidException() throws Exception {
    MockMvcBuilders.standaloneSetup(new ValidationController()).setControllerAdvice(handler).build()
      .perform(post("/validation-test").contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content("{\"fieldA\":\"\"}"))
      .andExpect(status().isBadRequest())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("code").value(400))
      .andExpect(jsonPath("validations[0].propertyName").value("fieldA"))
      .andExpect(jsonPath("validations[0].errorMessage").value("fieldA is required"));
  }

//  @Test
//  void testHandleDataIntegrityViolationException() {
//    var response = handler.handleDataIntegrityViolationException(new DataIntegrityViolationException("Duplicate key constraint"));
//    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
//    assertThat(response.getBody()).isNotNull();
//    assertThat(response.getBody().getValidations()).isNull();
//    assertThat(response.getBody().getMessage()).isEqualTo("Something went wrong. Please try again later.");
//  }

  @Test
  void testHandleRetryableException_returnsHumanReadableTimeout() {
    Request request = Request.create(Request.HttpMethod.POST, "https://uat-confinsr3.csulfinance.com/api/fou/v1/Generic/GetPagingObjectBySQL",
      java.util.Collections.emptyMap(), null, java.nio.charset.StandardCharsets.UTF_8, null);
    RetryableException retryable = new RetryableException(
      504,
      "Connect timed out executing POST https://uat-confinsr3.csulfinance.com/api/fou/v1/Generic/GetPagingObjectBySQL",
      Request.HttpMethod.POST,
      new java.net.SocketTimeoutException("Connect timed out"),
      1000L,
      request
    );

    var response = handler.handleRetryableException(retryable);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("Layanan Eksternal Timeout");
    assertThat(response.getBody().getCode()).isEqualTo(504);
    assertThat(response.getBody().getMessage()).contains("Koneksi ke layanan eksternal (Confins) sedang mengalami gangguan atau timeout");
    assertThat(response.getBody().getMessage()).doesNotContain("https://");
  }

  @Test
  void testHandleRuntimeException_withWrappedRetryableException_returnsHumanReadableTimeout() {
    Request request = Request.create(Request.HttpMethod.POST, "https://uat-confinsr3.csulfinance.com/api/fou/v1/Generic/GetPagingObjectBySQL",
      java.util.Collections.emptyMap(), null, java.nio.charset.StandardCharsets.UTF_8, null);
    RetryableException retryable = new RetryableException(
      504,
      "Connect timed out executing POST https://uat-confinsr3.csulfinance.com/api/fou/v1/Generic/GetPagingObjectBySQL",
      Request.HttpMethod.POST,
      new java.net.SocketTimeoutException("Connect timed out"),
      1000L,
      request
    );
    RuntimeException wrapped = new RuntimeException("Failed to call Confins API", retryable);

    var response = handler.handleRuntimeException(wrapped);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getCode()).isEqualTo(504);
    assertThat(response.getBody().getMessage()).contains("Koneksi ke layanan eksternal (Confins) sedang mengalami gangguan atau timeout");
  }

  @Test
  void testHandleFeignException_returnsHumanReadableError() {
    Request request = Request.create(Request.HttpMethod.POST, "https://uat-confinsr3.csulfinance.com/api/fou/v1/Generic/GetPagingObjectBySQL",
      java.util.Collections.emptyMap(), null, java.nio.charset.StandardCharsets.UTF_8, null);
    FeignException feignException = new FeignException.InternalServerError(
      "Internal Server Error",
      request,
      null,
      null
    );

    var response = handler.handleFeignException(feignException);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("Layanan Eksternal Error");
    assertThat(response.getBody().getMessage()).isEqualTo("Terjadi kendala komunikasi dengan layanan eksternal. Silakan coba kembali beberapa saat lagi.");
  }

  @RestController
  static class ValidationController {
    @PostMapping("/validation-test")
    public void validate(@Valid @RequestBody ValidationRequest request) {}
  }

  record ValidationRequest(@NotBlank(message = "fieldA is required") String fieldA) {}
}
