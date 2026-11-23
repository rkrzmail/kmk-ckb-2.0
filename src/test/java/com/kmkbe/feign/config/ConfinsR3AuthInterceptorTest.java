package com.kmkbe.feign.config;

import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ConfinsR3AuthInterceptorTest {

  @Test
  void apply_addsAdinsKeyHeader() {
    ConfinsR3AuthInterceptor interceptor = new ConfinsR3AuthInterceptor();
    ReflectionTestUtils.setField(interceptor, "adinsKey", "secret-adins-key-123");

    RequestTemplate template = new RequestTemplate();
    interceptor.apply(template);

    assertThat(template.headers()).containsKey("Adinskey");
    assertThat(template.headers().get("Adinskey")).containsExactly("secret-adins-key-123");
  }
}
