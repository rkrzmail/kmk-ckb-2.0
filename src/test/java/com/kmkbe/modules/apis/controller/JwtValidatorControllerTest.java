package com.kmkbe.modules.apis.controller;

import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.ValidationResponse;
import com.kmkbe.core.service.JwtValidatorService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit test JwtValidatorController (modules/apis):
 * semua jalur exception handler endpoint /auth/validate/{jwtToken}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtValidatorControllerTest {

  private static final String API_KEY = "app-key-1";
  private static final String JWT = "header.payload.signature";

  @Mock private JwtValidatorService jwtValidatorService;

  @InjectMocks
  private JwtValidatorController controller;

  private ValidationResponse validationResponse() {
    ValidationResponse response = new ValidationResponse();
    response.setBouwheer("bouwheer-1");
    response.setExp(9999999999L);
    return response;
  }

  @Test
  @DisplayName("Token valid -> 200 dengan data ValidationResponse")
  void validateTokenSuccess() {
    ValidationResponse response = validationResponse();
    when(jwtValidatorService.validate(API_KEY, JWT)).thenReturn(response);

    ResponseEntity<CommonResult<ValidationResponse>> result = controller.validateToken(JWT, API_KEY);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody().getCode()).isEqualTo(200);
    assertThat(result.getBody().getMessage()).isEqualTo("Token valid");
    assertThat(result.getBody().getData()).isSameAs(response);
    // CATATAN BUG (tidak diubah tanpa konfirmasi): controller tidak memanggil
    // result.success(...) sehingga isSuccess tetap false walau token valid.
    assertThat(result.getBody().isSuccess()).isFalse();
  }

  @Test
  @DisplayName("ExpiredJwtException -> 401 'Token expired'")
  void expiredTokenReturns401() {
    when(jwtValidatorService.validate(API_KEY, JWT))
      .thenThrow(new ExpiredJwtException(
        org.mockito.Mockito.mock(Header.class),
        org.mockito.Mockito.mock(Claims.class),
        "token expired at"));

    ResponseEntity<CommonResult<ValidationResponse>> result = controller.validateToken(JWT, API_KEY);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(result.getBody().isSuccess()).isFalse();
    assertThat(result.getBody().getCode()).isEqualTo(401);
    assertThat(result.getBody().getMessage()).contains("Token expired").contains("token expired at");
  }

  @Test
  @DisplayName("SignatureException -> 401 'Invalid token signature'")
  void invalidSignatureReturns401() {
    when(jwtValidatorService.validate(API_KEY, JWT))
      .thenThrow(new SignatureException("bad signature"));

    ResponseEntity<CommonResult<ValidationResponse>> result = controller.validateToken(JWT, API_KEY);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(result.getBody().getCode()).isEqualTo(401);
    assertThat(result.getBody().getMessage()).isEqualTo("Invalid token signature");
  }

  @Test
  @DisplayName("IllegalArgumentException -> 401 dengan pesan exception")
  void illegalArgumentReturns401() {
    when(jwtValidatorService.validate(API_KEY, JWT))
      .thenThrow(new IllegalArgumentException("ApiKey tidak ditemukan / tidak valid"));

    ResponseEntity<CommonResult<ValidationResponse>> result = controller.validateToken(JWT, API_KEY);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(result.getBody().getCode()).isEqualTo(401);
    assertThat(result.getBody().getMessage()).contains("ApiKey tidak ditemukan");
  }

  @Test
  @DisplayName("Exception lain -> 500 'Internal server error'")
  void unexpectedErrorReturns500() {
    when(jwtValidatorService.validate(API_KEY, JWT))
      .thenThrow(new NullPointerException("boom"));

    ResponseEntity<CommonResult<ValidationResponse>> result = controller.validateToken(JWT, API_KEY);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(result.getBody().getCode()).isEqualTo(500);
    assertThat(result.getBody().getMessage()).contains("Internal server error").contains("boom");
  }
}
