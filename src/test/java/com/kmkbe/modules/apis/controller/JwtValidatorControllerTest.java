package com.kmkbe.modules.apis.controller;

import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.ValidationResponse;
import com.kmkbe.core.service.JwtValidatorService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtValidatorControllerTest {

  private static final String API_KEY = "test-api-key";
  private static final String JWT_TOKEN = "test-jwt-token";

  @Mock
  private JwtValidatorService jwtValidatorService;

  @InjectMocks
  private JwtValidatorController controller;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(controller, "jwtValidatorService", jwtValidatorService);
  }

  @Test
  @DisplayName("validateToken returns 200 and SUCCESS when token is valid")
  void validateTokenSuccess() {
    ValidationResponse expectedResponse = new ValidationResponse();
    expectedResponse.setApiKey(API_KEY);
    expectedResponse.setBouwheer("BOUW-001");

    when(jwtValidatorService.validate(API_KEY, JWT_TOKEN)).thenReturn(expectedResponse);

    ResponseEntity<CommonResult<ValidationResponse>> response = controller.validateToken(JWT_TOKEN, API_KEY);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getCode()).isEqualTo(200);
    assertThat(response.getBody().getMessage()).isEqualTo("Token valid");
    assertThat(response.getBody().getData()).isSameAs(expectedResponse);
    verify(jwtValidatorService).validate(API_KEY, JWT_TOKEN);
  }

  @Test
  @DisplayName("validateToken returns 401 when ExpiredJwtException is thrown")
  void validateTokenExpired() {
    when(jwtValidatorService.validate(API_KEY, JWT_TOKEN))
      .thenThrow(new ExpiredJwtException(null, null, "Token has expired"));

    ResponseEntity<CommonResult<ValidationResponse>> response = controller.validateToken(JWT_TOKEN, API_KEY);

    assertThat(response.getStatusCode().value()).isEqualTo(401);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getCode()).isEqualTo(401);
    assertThat(response.getBody().getMessage()).contains("Token expired");
  }

  @Test
  @DisplayName("validateToken returns 401 when SignatureException is thrown")
  void validateTokenInvalidSignature() {
    when(jwtValidatorService.validate(API_KEY, JWT_TOKEN))
      .thenThrow(new SignatureException("JWT signature does not match"));

    ResponseEntity<CommonResult<ValidationResponse>> response = controller.validateToken(JWT_TOKEN, API_KEY);

    assertThat(response.getStatusCode().value()).isEqualTo(401);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getCode()).isEqualTo(401);
    assertThat(response.getBody().getMessage()).isEqualTo("Invalid token signature");
  }

  @Test
  @DisplayName("validateToken returns 401 when IllegalArgumentException is thrown")
  void validateTokenIllegalArgument() {
    when(jwtValidatorService.validate(API_KEY, JWT_TOKEN))
      .thenThrow(new IllegalArgumentException("Malformed token"));

    ResponseEntity<CommonResult<ValidationResponse>> response = controller.validateToken(JWT_TOKEN, API_KEY);

    assertThat(response.getStatusCode().value()).isEqualTo(401);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getCode()).isEqualTo(401);
    assertThat(response.getBody().getMessage()).isEqualTo("Malformed token");
  }

  @Test
  @DisplayName("validateToken returns 500 when generic Exception is thrown")
  void validateTokenInternalServerError() {
    when(jwtValidatorService.validate(API_KEY, JWT_TOKEN))
      .thenThrow(new RuntimeException("Database down"));

    ResponseEntity<CommonResult<ValidationResponse>> response = controller.validateToken(JWT_TOKEN, API_KEY);

    assertThat(response.getStatusCode().value()).isEqualTo(500);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getCode()).isEqualTo(500);
    assertThat(response.getBody().getMessage()).contains("Internal server error: Database down");
  }
}
