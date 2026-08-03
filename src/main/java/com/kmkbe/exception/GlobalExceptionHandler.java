package com.kmkbe.exception;

import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.exception.LoanDocMandatoryException;
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
      .title("Error Business Exception")
      .code(e.getCode())
      .message(e.getMessage())
      .build();
    return createResponseEntity(e.getHttpStatus(), errorResponse);
  }

  // ==========================================
  // 2. FRAMEWORK VALIDATION & INFRASTRUCTURE EXCEPTIONS
  // ==========================================

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
      .message(HttpStatus.BAD_REQUEST.getReasonPhrase())
      .validations(validations)
      .build();

    return createResponseEntity(HttpStatus.BAD_REQUEST, errorResponse);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
    log.error("[Exception] DataIntegrityViolationException: ", e);
    ErrorResponse errorResponse = ErrorResponse.builder()
      .code(HttpStatus.CONFLICT.value())
      .message("Violates foreign key constraint")
      .build();
    return createResponseEntity(HttpStatus.CONFLICT, errorResponse);
  }

  @ExceptionHandler(TimeoutException.class)
  public ResponseEntity<ErrorResponse> handleTimeoutException(TimeoutException e) {
    log.error("[Exception] TimeoutException: ", e);
    ErrorResponse errorResponse = ErrorResponse.builder()
      .code(HttpStatus.SERVICE_UNAVAILABLE.value())
      .message("Timeout Exception")
      .build();
    return createResponseEntity(HttpStatus.REQUEST_TIMEOUT, errorResponse);
  }

  @ExceptionHandler(HttpServerErrorException.class)
  public ResponseEntity<ErrorResponse> handleHttpServerErrorException(HttpServerErrorException e) {
    log.error("[Exception] HttpServerErrorException: ", e);
    ErrorResponse errorResponse = ErrorResponse.builder()
      .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
      .message("Internal server error")
      .build();
    return createResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, errorResponse);
  }

  // ==========================================
  // 3. FALLBACK GENERAL EXCEPTION (Paling Bawah)
  // ==========================================

  /**
   * Menggunakan hirarki paling dasar dari Unchecked Exception.
   * Ditaruh di paling bawah agar tidak menangkap/meng-override custom exception di atas.
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
    log.error("[Fallback Exception] RuntimeException Caught: ", e);
    ErrorResponse errorResponse = ErrorResponse.builder()
      .code(HttpStatus.BAD_REQUEST.value())
      .message(e.getMessage())
      .build();
    return createResponseEntity(HttpStatus.BAD_REQUEST, errorResponse);
  }

  // ==========================================
  // HELPER METHOD
  // ==========================================

  private ResponseEntity<ErrorResponse> createResponseEntity(HttpStatus status, ErrorResponse errorResponse) {
    if (errorResponse.getTitle() == null) {
      errorResponse.setTitle("System Error");
    }
    return ResponseEntity.status(status).body(errorResponse);
  }
}

