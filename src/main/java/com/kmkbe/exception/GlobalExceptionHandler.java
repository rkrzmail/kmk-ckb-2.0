package com.kmkbe.exception;

import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.exception.LoanDocMandatoryException;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;
import java.util.concurrent.TimeoutException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String DEFAULT_ERROR_MESSAGE = "Something went wrong. Please try again later.";

  @ExceptionHandler(LoanDocMandatoryException.class)
  public ResponseEntity<CommonResult<LoanDocMandatoryException>> handleLoanDocMandatoryException(
    LoanDocMandatoryException exception
  ) {
    log.error("[Exception] LoanDocMandatoryException: {}", exception.getMessage());
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(new CommonResult<LoanDocMandatoryException>().fail(
        HttpStatus.BAD_REQUEST.value(),
        exception.getMessage()
      ));
  }

  @ExceptionHandler(CommonInvalidException.class)
  public ResponseEntity<CommonResult<Object>> handleLoginException(
    CommonInvalidException exception
  ) {
    log.error("[Exception] CommonInvalidException: {}", exception.getHeaderMessage());
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(new CommonResult<>().fail(
        HttpStatus.BAD_REQUEST.value(),
        exception.getHeaderMessage(),
        exception.getPayload()
      ));
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
    log.error("[Exception] BusinessException: code={}, message={}", e.getCode(), e.getMessage());
    ErrorResponse errorResponse = ErrorResponse.builder()
      .title("Business Error")
      .code(e.getCode())
      .message(e.getMessage())
      .build();
    return createResponseEntity(e.getHttpStatus(), errorResponse);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
    log.error("[Exception] Validation Error: {}", e.getMessage());

    List<ErrorValidationResponse> validations = e.getBindingResult().getFieldErrors().stream()
      .map(fieldError -> ErrorValidationResponse.builder()
        .propertyName(fieldError.getField())
        .errorMessage(fieldError.getDefaultMessage())
        .build())
      .toList();

    ErrorResponse errorResponse = ErrorResponse.builder()
      .code(HttpStatus.BAD_REQUEST.value())
      .message("Validation failed")
      .validations(validations)
      .build();

    return createResponseEntity(HttpStatus.BAD_REQUEST, errorResponse);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
    log.error("[Exception] DataIntegrityViolationException: ", e);

    ErrorResponse errorResponse = ErrorResponse.builder()
      .code(HttpStatus.CONFLICT.value())
      .message(DEFAULT_ERROR_MESSAGE)
      .build();
    return createResponseEntity(HttpStatus.CONFLICT, errorResponse);
  }

  @ExceptionHandler(TimeoutException.class)
  public ResponseEntity<ErrorResponse> handleTimeoutException(TimeoutException e) {
    log.error("[Exception] TimeoutException: ", e);
    ErrorResponse errorResponse = ErrorResponse.builder()
      .code(HttpStatus.SERVICE_UNAVAILABLE.value())
      .message(DEFAULT_ERROR_MESSAGE)
      .build();
    return createResponseEntity(HttpStatus.REQUEST_TIMEOUT, errorResponse);
  }

  @ExceptionHandler(HttpServerErrorException.class)
  public ResponseEntity<ErrorResponse> handleHttpServerErrorException(HttpServerErrorException e) {
    log.error("[Exception] HttpServerErrorException: ", e);
    ErrorResponse errorResponse = ErrorResponse.builder()
      .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
      .message(DEFAULT_ERROR_MESSAGE)
      .build();
    return createResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, errorResponse);
  }

  @ExceptionHandler(RetryableException.class)
  public ResponseEntity<ErrorResponse> handleRetryableException(RetryableException e) {
    log.error("[Exception] Feign RetryableException: {}", e.getMessage());
    ErrorResponse errorResponse = ErrorResponse.builder()
      .title("Layanan Eksternal Timeout")
      .code(HttpStatus.GATEWAY_TIMEOUT.value())
      .message("Koneksi ke layanan eksternal (Confins) sedang mengalami gangguan atau timeout. Silakan coba kembali beberapa saat lagi.")
      .build();
    return createResponseEntity(HttpStatus.GATEWAY_TIMEOUT, errorResponse);
  }

  @ExceptionHandler(FeignException.class)
  public ResponseEntity<ErrorResponse> handleFeignException(FeignException e) {
    log.error("[Exception] FeignException: status={}, message={}", e.status(), e.getMessage());
    HttpStatus status = e.status() >= 500 ? HttpStatus.BAD_GATEWAY : HttpStatus.SERVICE_UNAVAILABLE;
    ErrorResponse errorResponse = ErrorResponse.builder()
      .title("Layanan Eksternal Error")
      .code(status.value())
      .message("Terjadi kendala komunikasi dengan layanan eksternal. Silakan coba kembali beberapa saat lagi.")
      .build();
    return createResponseEntity(status, errorResponse);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
    log.error("[Fallback Exception] RuntimeException Caught: ", e);

    if (isConnectionTimeout(e)) {
      ErrorResponse errorResponse = ErrorResponse.builder()
        .title("Layanan Eksternal Timeout")
        .code(HttpStatus.GATEWAY_TIMEOUT.value())
        .message("Koneksi ke layanan eksternal (Confins) sedang mengalami gangguan atau timeout. Silakan coba kembali beberapa saat lagi.")
        .build();
      return createResponseEntity(HttpStatus.GATEWAY_TIMEOUT, errorResponse);
    }

    ErrorResponse errorResponse = ErrorResponse.builder()
      .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
      .message(DEFAULT_ERROR_MESSAGE)
      .build();
    return createResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
    log.error("[Critical Fallback Exception] Exception Caught: ", e);

    if (isConnectionTimeout(e)) {
      ErrorResponse errorResponse = ErrorResponse.builder()
        .title("Layanan Eksternal Timeout")
        .code(HttpStatus.GATEWAY_TIMEOUT.value())
        .message("Koneksi ke layanan eksternal (Confins) sedang mengalami gangguan atau timeout. Silakan coba kembali beberapa saat lagi.")
        .build();
      return createResponseEntity(HttpStatus.GATEWAY_TIMEOUT, errorResponse);
    }

    ErrorResponse errorResponse = ErrorResponse.builder()
      .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
      .message(DEFAULT_ERROR_MESSAGE)
      .build();
    return createResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, errorResponse);
  }

  private boolean isConnectionTimeout(Throwable throwable) {
    Throwable current = throwable;
    while (current != null) {
      if (current instanceof RetryableException
          || current instanceof java.net.SocketTimeoutException
          || current instanceof java.net.ConnectException
          || current instanceof java.net.UnknownHostException
          || current instanceof org.apache.hc.client5.http.ConnectTimeoutException
          || (current.getMessage() != null && (
              current.getMessage().contains("Connect timed out")
              || current.getMessage().contains("Read timed out")
              || current.getMessage().contains("Connection refused")
              || current.getMessage().contains("feign.RetryableException")
          ))) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }

  private ResponseEntity<ErrorResponse> createResponseEntity(HttpStatus status, ErrorResponse errorResponse) {
    if (errorResponse.getTitle() == null) {
      errorResponse.setTitle("System Error");
    }
    return ResponseEntity.status(status).body(errorResponse);
  }
}

