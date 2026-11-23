package com.kmkbe.core.middleware;

import com.kmkbe.core.utils.HttpUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

class HttpRequestResponseLogFilterTest {
  private final HttpRequestResponseLogFilter filter = new HttpRequestResponseLogFilter();

  @Test void multipartPassesThrough() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("POST", "/upload");
    req.addHeader("Content-Type", "multipart/form-data");
    FilterChain chain = mock(FilterChain.class);
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);
    org.mockito.Mockito.verify(chain).doFilter(req, res);
  }

  @Test void normalRequestPassesThroughWrappers() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/x");
    req.setQueryString("a=1");
    FilterChain chain = (r, res) -> { ((HttpServletResponse) res).setStatus(200); res.getWriter().write("ok"); };
    assertThatCode(() -> filter.doFilter(req, new MockHttpServletResponse(), chain)).doesNotThrowAnyException();
  }

  @Test void shouldLogWhenInfoEnabled() {
    assertThat(filter.shouldLog(new MockHttpServletRequest())).isTrue();
  }
}
