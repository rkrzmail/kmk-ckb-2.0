package com.kmkbe.feign.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsulAuthConfigTest {

  @Mock
  private CsulTokenManager tokenManager;

  private CsulAuthConfig config;

  @BeforeEach
  void setUp() {
    config = new CsulAuthConfig(tokenManager);
  }

  @Test
  void requestInterceptor_injectsTokenWhenUrlContainsWebhookToken() {
    when(tokenManager.getToken()).thenReturn("Bearer test-token-123");

    RequestInterceptor interceptor = config.requestInterceptor();
    RequestTemplate template = new RequestTemplate();
    template.uri("/api/v1/webhook/token");

    interceptor.apply(template);

    assertThat(template.headers()).containsKey("Authorization");
    assertThat(template.headers().get("Authorization")).containsExactly("Bearer test-token-123");
    verify(tokenManager).getToken();
  }

  @Test
  void requestInterceptor_skipsTokenWhenUrlDoesNotContainWebhookToken() {
    RequestInterceptor interceptor = config.requestInterceptor();
    RequestTemplate template = new RequestTemplate();
    template.uri("/api/v1/sbu/ckb/vendor");

    interceptor.apply(template);

    assertThat(template.headers()).doesNotContainKey("Authorization");
    verifyNoInteractions(tokenManager);
  }
}
