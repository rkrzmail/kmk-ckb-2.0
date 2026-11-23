package com.kmkbe.core.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.*;

class UnauthorizedEntryPointTest {
  @Test void commenceWritesUnauthorizedJson() throws Exception {
    var ep = new UnauthorizedEntryPoint(new ObjectMapper());
    var req = new MockHttpServletRequest();
    var res = new MockHttpServletResponse();
    ep.commence(req, res, new BadCredentialsException("bad"));
    assertThat(res.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(res.getContentAsString()).contains("Invalid login session");
  }

  @Test void commenceWrapsIOException() {
    var ep = new UnauthorizedEntryPoint(new ObjectMapper());
    var req = new MockHttpServletRequest();
    HttpServletResponse res = org.mockito.Mockito.mock(HttpServletResponse.class);
    try {
      org.mockito.Mockito.doThrow(new java.io.IOException("io")).when(res).getOutputStream();
      assertThatThrownBy(() -> ep.commence(req, res, new BadCredentialsException("bad")))
        .isInstanceOf(RuntimeException.class).hasMessageContaining("Failed Authentication Response");
    } catch (Exception e) { fail(e); }
  }
}
