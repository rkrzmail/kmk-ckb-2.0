package com.kmkbe.core.utils;

import com.kmkbe.core.middleware.HttpServletRequestCopier;
import com.kmkbe.core.middleware.HttpServletResponseCopier;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

class HttpUtilsCoverageTest {

  @Test void utilityConstructors() {
    assertThat(new HttpUtils()).isNotNull();
    assertThat(new ExceptionUtils()).isNotNull();
    assertThat(new ObjectUtils()).isNotNull();
    assertThat(new AESUtils()).isNotNull();
    assertThat(new FileUtils()).isNotNull();
    assertThat(new CommonFormattingUtils()).isNotNull();
    assertThat(new UriUtils()).isNotNull();
    assertThat(new ExecutionTimer()).isNotNull();
    assertThat(new MD5Utils()).isNotNull();
    assertThat(new DateTimeUtils()).isNotNull();
  }

  @Test void createLogsWithWrappers() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("POST", "/x");
    req.setQueryString("token=secret&q=1");
    req.setContentType("application/json");
    req.setCharacterEncoding("UTF-8");
    req.addHeader("Authorization", "Bearer tok");
    req.addHeader("API-Key", "key");
    req.setSession(new MockHttpSession());
    req.setContent("{\"password\":\"secret\",\"name\":\"n\"}".getBytes(StandardCharsets.UTF_8));
    HttpServletRequestCopier wrapper = new HttpServletRequestCopier(req);
    var log = HttpUtils.createRequestLog(wrapper, 1000);
    assertThat(log).containsKeys("queryString","headers","payload","client","sessionId","user");
    assertThat(String.valueOf(log.get("queryString"))).contains("[REDACTED]");
    assertThat(String.valueOf(log.get("headers"))).doesNotContain("Bearer tok");
    assertThat(String.valueOf(log.get("payload"))).doesNotContain("secret");

    HttpServletResponseCopier res = new HttpServletResponseCopier(new MockHttpServletResponse());
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    res.getOutputStream().write("{\"token\":\"abc\"}".getBytes(StandardCharsets.UTF_8));
    var responseLog = HttpUtils.createResponseLog(res, 1000);
    assertThat(responseLog).containsKeys("statusCode","contentType","payload");
    assertThat(String.valueOf(responseLog.get("payload"))).doesNotContain("abc");
  }

  @Test void sanitizeAndHeaders() {
    assertThat(HttpUtils.sanitizeQueryString(null)).isNull();
    assertThat(HttpUtils.sanitizeQueryString("")).isEmpty();
    assertThat(HttpUtils.sanitizeQueryString("token=abc")).contains("[REDACTED]");
    assertThat(HttpUtils.sanitizePayload("plain")).isEqualTo("plain");
    assertThat(HttpUtils.sanitizePayload("{\"password\":\"x\"}")).doesNotContain("x");

    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("Authorization", "Bearer tok");
    req.addHeader("rtoken", "r");
    assertThat(HttpUtils.getHeaderAuthorization(req)).isEqualTo("Bearer tok");
    assertThat(HttpUtils.getHeaderBearerToken(req)).isEqualTo("tok");
    assertThat(HttpUtils.getHeaderRefreshToken(req)).isEqualTo("r");
    assertThat(HttpUtils.getHeaderApiKey(req)).isNull();
  }
}
