package com.kmkbe.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class CustomFilterTest {

  private final CustomFilter customFilter = new CustomFilter();

  @Test
  void initCompletesWithoutInteractingWithFilterConfig() throws Exception {
    FilterConfig filterConfig = mock(FilterConfig.class);

    customFilter.init(filterConfig);

    verifyNoInteractions(filterConfig);
  }

  @Test
  void doFilterCastsToHttpRequestAndResponseThenContinuesChain() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    customFilter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    verifyNoInteractions(request, response);
  }

  @Test
  void destroyCompletesWithoutError() {
    customFilter.destroy();
  }
}
