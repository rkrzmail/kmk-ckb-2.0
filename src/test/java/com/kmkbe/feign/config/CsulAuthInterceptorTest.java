package com.kmkbe.feign.config;

import com.kmkbe.feign.client.ConfinsR3FeignClient;
import com.kmkbe.feign.client.CsulVendorFeignClient;
import feign.RequestTemplate;
import feign.Target;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsulAuthInterceptorTest {

  @Mock
  private CsulTokenManager tokenManager;

  private CsulAuthInterceptor interceptor;

  @BeforeEach
  void setUp() {
    interceptor = new CsulAuthInterceptor(tokenManager);
  }

  @Test
  void apply_skipsWhenUrlContainsWebhookToken() {
    RequestTemplate template = new RequestTemplate();
    template.uri("/api/v1/webhook/token");
    template.feignTarget(new Target.HardCodedTarget<>(CsulVendorFeignClient.class, "csulVendorClient", "http://example.com"));

    interceptor.apply(template);

    assertThat(template.headers()).doesNotContainKey("Authorization");
    verifyNoInteractions(tokenManager);
  }

  @Test
  void apply_skipsWhenUrlEndsWithToken() {
    RequestTemplate template = new RequestTemplate();
    template.uri("/api/v1/auth/token");
    template.feignTarget(new Target.HardCodedTarget<>(CsulVendorFeignClient.class, "csulVendorClient", "http://example.com"));

    interceptor.apply(template);

    assertThat(template.headers()).doesNotContainKey("Authorization");
    verifyNoInteractions(tokenManager);
  }

  @Test
  void apply_skipsWhenTargetNameIsCsulAuthFeignClient() {
    RequestTemplate template = new RequestTemplate();
    template.uri("/api/v1/other/path");
    template.feignTarget(new Target.HardCodedTarget<>(CsulVendorFeignClient.class, "csulAuthFeignClient", "http://example.com"));

    interceptor.apply(template);

    assertThat(template.headers()).doesNotContainKey("Authorization");
    verifyNoInteractions(tokenManager);
  }

  @Test
  void apply_skipsWhenTargetTypeIsConfinsR3FeignClient() {
    RequestTemplate template = new RequestTemplate();
    template.uri("/api/v1/other/path");
    template.feignTarget(new Target.HardCodedTarget<>(ConfinsR3FeignClient.class, "confinsClient", "http://example.com"));

    interceptor.apply(template);

    assertThat(template.headers()).doesNotContainKey("Authorization");
    verifyNoInteractions(tokenManager);
  }

  @Test
  void apply_injectsAuthorizationHeaderForNormalRequest() {
    when(tokenManager.getToken()).thenReturn("Bearer dummy-jwt");

    RequestTemplate template = new RequestTemplate();
    template.uri("/api/v1/sbu/ckb/vendor");
    template.feignTarget(new Target.HardCodedTarget<>(CsulVendorFeignClient.class, "csulVendorClient", "http://example.com"));

    interceptor.apply(template);

    assertThat(template.headers()).containsKey("Authorization");
    assertThat(template.headers().get("Authorization")).containsExactly("Bearer dummy-jwt");
    verify(tokenManager).getToken();
  }
}
