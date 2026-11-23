package com.kmkbe.feign.config;

import com.kmkbe.feign.exception.VendorNotFoundException;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeignErrorDecoderTest {

  @Mock
  private CsulTokenManager tokenManager;

  private FeignErrorDecoder decoder;

  @BeforeEach
  void setUp() {
    decoder = new FeignErrorDecoder(tokenManager);
  }

  private Request createDummyRequest() {
    return Request.create(Request.HttpMethod.GET, "https://api.example.com/test",
      Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
  }

  @Test
  void decode_status401_triggersReloginAndDelegatesWhenBodyIsNull() {
    Response response = Response.builder()
      .status(401)
      .reason("Unauthorized")
      .request(createDummyRequest())
      .headers(Map.of())
      .build();

    Exception exception = decoder.decode("testMethod", response);

    verify(tokenManager).clearTokenAndRelogin();
    assertThat(exception).isInstanceOf(FeignException.Unauthorized.class);
  }

  @Test
  void decode_status401_whenReloginFails_returnsRuntimeException() {
    doThrow(new RuntimeException("Relogin error")).when(tokenManager).clearTokenAndRelogin();

    Response response = Response.builder()
      .status(401)
      .reason("Unauthorized")
      .request(createDummyRequest())
      .headers(Map.of())
      .build();

    Exception exception = decoder.decode("testMethod", response);

    verify(tokenManager).clearTokenAndRelogin();
    assertThat(exception)
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Failed to re-authenticate after token expiration")
      .hasCauseInstanceOf(RuntimeException.class);
  }

  @Test
  void decode_whenBodyContainsStatusFailed_returnsVendorNotFoundException() {
    String jsonBody = "{\"status\":\"FAILED\",\"message\":\"No vendor registered\"}";
    Response response = Response.builder()
      .status(400)
      .reason("Bad Request")
      .request(createDummyRequest())
      .headers(Map.of())
      .body(jsonBody, StandardCharsets.UTF_8)
      .build();

    Exception exception = decoder.decode("testMethod", response);

    assertThat(exception)
      .isInstanceOf(VendorNotFoundException.class)
      .hasMessage("Vendor tidak ditemukan");
    verifyNoInteractions(tokenManager);
  }

  @Test
  void decode_whenBodyContainsStatusCode404_returnsVendorNotFoundException() {
    String jsonBody = "{\"statusCode\":404,\"message\":\"Resource not found\"}";
    Response response = Response.builder()
      .status(404)
      .reason("Not Found")
      .request(createDummyRequest())
      .headers(Map.of())
      .body(jsonBody, StandardCharsets.UTF_8)
      .build();

    Exception exception = decoder.decode("testMethod", response);

    assertThat(exception)
      .isInstanceOf(VendorNotFoundException.class)
      .hasMessage("Vendor tidak ditemukan");
  }

  @Test
  void decode_whenBodyDoesNotContainFailedPattern_delegatesToDefaultDecoder() {
    String jsonBody = "{\"status\":\"SUCCESS\",\"data\":null}";
    Response response = Response.builder()
      .status(500)
      .reason("Internal Server Error")
      .request(createDummyRequest())
      .headers(Map.of())
      .body(jsonBody, StandardCharsets.UTF_8)
      .build();

    Exception exception = decoder.decode("testMethod", response);

    assertThat(exception).isInstanceOf(FeignException.InternalServerError.class);
  }

  @Test
  void decode_whenBodyReadThrowsIOException_fallsBackToDefaultDecoder() throws IOException {
    Response.Body mockBody = mock(Response.Body.class);
    when(mockBody.asReader(StandardCharsets.UTF_8)).thenThrow(new IOException("Disk error"));
    when(mockBody.asInputStream()).thenReturn(new java.io.ByteArrayInputStream("fallback".getBytes(StandardCharsets.UTF_8)));

    Response response = Response.builder()
      .status(500)
      .reason("Internal Server Error")
      .request(createDummyRequest())
      .headers(Map.of())
      .body(mockBody)
      .build();

    Exception exception = decoder.decode("testMethod", response);

    assertThat(exception).isInstanceOf(FeignException.InternalServerError.class);
  }
}
