package com.kmkbe.feign.config;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConfinsR3ErrorDecoderTest {

  @Test
  void decode_logsAndDelegatesToDefaultDecoder() {
    ConfinsR3ErrorDecoder decoder = new ConfinsR3ErrorDecoder();
    Request request = Request.create(Request.HttpMethod.POST, "https://confins.example/api",
      Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
    Response response = Response.builder()
      .status(404)
      .reason("Not Found")
      .request(request)
      .headers(Map.of())
      .body("{\"error\":\"Not Found\"}", StandardCharsets.UTF_8)
      .build();

    Exception exception = decoder.decode("testMethod", response);

    assertThat(exception).isInstanceOf(FeignException.NotFound.class);
  }
}
