package com.kmkbe.core.utils;

import com.kmkbe.core.exception.LoanDocMandatoryException;
import com.kmkbe.exception.BusinessException;
import org.hibernate.PropertyValueException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import java.net.ConnectException;
import java.security.SignatureException;
import java.net.SocketTimeoutException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ExceptionUtilsCoverageTest {

  @Test void additionalExceptionMappings() {
    assertThat(ExceptionUtils.handleException(new LoanDocMandatoryException("doc"), null).getStatusCode())
      .isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(ExceptionUtils.handleException(new SignatureException(), null).getStatusCode())
      .isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(ExceptionUtils.handleException(new DataIntegrityViolationException("dup"), null).getStatusCode())
      .isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(ExceptionUtils.handleException(new DataIntegrityViolationException("dup", new PropertyValueException("m","e","p")), null).getStatusCode())
      .isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(ExceptionUtils.handleException(new NoSuchElementException("missing"), null).getStatusCode())
      .isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(ExceptionUtils.handleException(new BusinessException(HttpStatus.I_AM_A_TEAPOT, 1, "tea"), null).getStatusCode())
      .isEqualTo(HttpStatus.I_AM_A_TEAPOT);
    assertThat(ExceptionUtils.handleException(new RuntimeException("wrap", new SocketTimeoutException("Read timed out")), null).getStatusCode())
      .isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    assertThat(ExceptionUtils.handleException(new RuntimeException("wrap", new ConnectException("Connection refused")), mock(WebRequest.class)).getStatusCode())
      .isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
  }
}
