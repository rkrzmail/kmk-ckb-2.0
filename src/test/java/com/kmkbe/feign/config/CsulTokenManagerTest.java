package com.kmkbe.feign.config;

import com.kmkbe.exception.BusinessException;
import com.kmkbe.feign.model.dto.CsulPostLoginDto;
import com.kmkbe.feign.model.request.CsulPostLoginRequest;
import com.kmkbe.helpers.constant.ErrorConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsulTokenManagerTest {

  @Mock
  private RestTemplate restTemplate;

  private CsulTokenManager tokenManager;

  @BeforeEach
  void setUp() {
    tokenManager = new CsulTokenManager(restTemplate);
    ReflectionTestUtils.setField(tokenManager, "apiBaseCKB", "https://ckb.example.com");
    ReflectionTestUtils.setField(tokenManager, "username", "csul_user");
    ReflectionTestUtils.setField(tokenManager, "password", "csul_pass");
    ReflectionTestUtils.setField(tokenManager, "clientSecret", "csul_secret");
  }

  @Test
  void getToken_success_returnsBearerToken() {
    CsulPostLoginDto.AuthData authData = new CsulPostLoginDto.AuthData();
    authData.setToken("my-secret-token");
    authData.setTokenType("Bearer");
    authData.setExpiresIn(3600);

    CsulPostLoginDto dto = CsulPostLoginDto.builder()
      .success(true)
      .statusCode(200)
      .data(authData)
      .build();

    when(restTemplate.postForEntity(eq("https://ckb.example.com/api/v1/webhook/token"), any(HttpEntity.class), eq(CsulPostLoginDto.class)))
      .thenReturn(ResponseEntity.ok(dto));

    String token = tokenManager.getToken();

    assertThat(token).isEqualTo("Bearer my-secret-token");

    ArgumentCaptor<HttpEntity<CsulPostLoginRequest>> captor = ArgumentCaptor.forClass(HttpEntity.class);
    verify(restTemplate).postForEntity(eq("https://ckb.example.com/api/v1/webhook/token"), captor.capture(), eq(CsulPostLoginDto.class));
    HttpEntity<CsulPostLoginRequest> capturedEntity = captor.getValue();
    assertThat(capturedEntity.getBody()).isNotNull();
    assertThat(capturedEntity.getBody().getUsername()).isEqualTo("csul_user");
    assertThat(capturedEntity.getBody().getPassword()).isEqualTo("csul_pass");
    assertThat(capturedEntity.getBody().getClientSecret()).isEqualTo("csul_secret");
  }

  @Test
  void clearTokenAndRelogin_refreshesToken() {
    CsulPostLoginDto.AuthData authData1 = new CsulPostLoginDto.AuthData();
    authData1.setToken("token-1");
    CsulPostLoginDto dto1 = CsulPostLoginDto.builder().success(true).statusCode(200).data(authData1).build();

    CsulPostLoginDto.AuthData authData2 = new CsulPostLoginDto.AuthData();
    authData2.setToken("token-2");
    CsulPostLoginDto dto2 = CsulPostLoginDto.builder().success(true).statusCode(200).data(authData2).build();

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(CsulPostLoginDto.class)))
      .thenReturn(ResponseEntity.ok(dto1))
      .thenReturn(ResponseEntity.ok(dto2));

    String firstToken = tokenManager.getToken();
    assertThat(firstToken).isEqualTo("Bearer token-1");

    tokenManager.clearTokenAndRelogin();
    String secondToken = tokenManager.getToken();
    assertThat(secondToken).isEqualTo("Bearer token-2");
  }

  @Test
  void getToken_whenResponseBodyIsNull_throwsBusinessException() {
    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(CsulPostLoginDto.class)))
      .thenReturn(ResponseEntity.ok(null));

    assertThatThrownBy(() -> tokenManager.getToken())
      .isInstanceOf(BusinessException.class)
      .satisfies(ex -> {
        BusinessException be = (BusinessException) ex;
        assertThat(be.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(be.getCode()).isEqualTo(ErrorConstant.ERROR_CODE_80);
      });
  }

  @Test
  void getToken_whenResponseDataIsNull_throwsBusinessException() {
    CsulPostLoginDto dto = CsulPostLoginDto.builder()
      .success(true)
      .statusCode(200)
      .data(null)
      .build();

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(CsulPostLoginDto.class)))
      .thenReturn(ResponseEntity.ok(dto));

    assertThatThrownBy(() -> tokenManager.getToken())
      .isInstanceOf(BusinessException.class)
      .satisfies(ex -> {
        BusinessException be = (BusinessException) ex;
        assertThat(be.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(be.getCode()).isEqualTo(ErrorConstant.ERROR_CODE_80);
      });
  }

  @Test
  void getToken_whenTokenInResponseDataIsNull_throwsBusinessException() {
    CsulPostLoginDto.AuthData authData = new CsulPostLoginDto.AuthData();
    authData.setToken(null);

    CsulPostLoginDto dto = CsulPostLoginDto.builder()
      .success(true)
      .statusCode(200)
      .data(authData)
      .build();

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(CsulPostLoginDto.class)))
      .thenReturn(ResponseEntity.ok(dto));

    assertThatThrownBy(() -> tokenManager.getToken())
      .isInstanceOf(BusinessException.class)
      .satisfies(ex -> {
        BusinessException be = (BusinessException) ex;
        assertThat(be.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(be.getCode()).isEqualTo(ErrorConstant.ERROR_CODE_80);
      });
  }

  @Test
  void getToken_whenBusinessExceptionThrown_rethrownDirectly() {
    BusinessException customEx = new BusinessException(HttpStatus.BAD_REQUEST, 99, "Custom error");
    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(CsulPostLoginDto.class)))
      .thenThrow(customEx);

    assertThatThrownBy(() -> tokenManager.getToken())
      .isSameAs(customEx);
  }

  @Test
  void getToken_whenGenericExceptionThrown_wrappedInBusinessException() {
    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(CsulPostLoginDto.class)))
      .thenThrow(new RestClientException("Connection refused"));

    assertThatThrownBy(() -> tokenManager.getToken())
      .isInstanceOf(BusinessException.class)
      .satisfies(ex -> {
        BusinessException be = (BusinessException) ex;
        assertThat(be.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(be.getCode()).isEqualTo(ErrorConstant.ERROR_CODE_80);
        assertThat(be.getMessage()).contains("Authentication failed response data was empty");
      });
  }
}
