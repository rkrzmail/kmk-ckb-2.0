package com.kmkbe.feign.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.RedirectStrategy;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;

class FeignHttpClientConfigTest {

  @Test
  void httpClient_buildsNonNullClient() throws Exception {
    FeignHttpClientConfig config = new FeignHttpClientConfig();
    try (CloseableHttpClient client = config.httpClient()) {
      assertThat(client).isNotNull();
    }
  }

  @Test
  void redirectStrategy_redirectsExpectedStatusesOnly() throws Exception {
    FeignHttpClientConfig config = new FeignHttpClientConfig();
    Class<?> anonymousClass = Class.forName("com.kmkbe.feign.config.FeignHttpClientConfig$1");
    Constructor<?> ctor = anonymousClass.getDeclaredConstructors()[0];
    ctor.setAccessible(true);
    RedirectStrategy strategy = (RedirectStrategy) ctor.newInstance(config);

    HttpResponse res301 = new BasicHttpResponse(301);
    HttpResponse res302 = new BasicHttpResponse(302);
    HttpResponse res303 = new BasicHttpResponse(303);
    HttpResponse res307 = new BasicHttpResponse(307);
    HttpResponse res200 = new BasicHttpResponse(200);
    HttpResponse res404 = new BasicHttpResponse(404);
    HttpResponse res500 = new BasicHttpResponse(500);

    assertThat(strategy.isRedirected(null, res301, null)).isTrue();
    assertThat(strategy.isRedirected(null, res302, null)).isTrue();
    assertThat(strategy.isRedirected(null, res303, null)).isTrue();
    assertThat(strategy.isRedirected(null, res307, null)).isTrue();

    assertThat(strategy.isRedirected(null, res200, null)).isFalse();
    assertThat(strategy.isRedirected(null, res404, null)).isFalse();
    assertThat(strategy.isRedirected(null, res500, null)).isFalse();

    assertThat(strategy.getLocationURI(null, null, null)).isNull();
  }
}
