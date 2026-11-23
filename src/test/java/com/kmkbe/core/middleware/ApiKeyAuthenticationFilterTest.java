package com.kmkbe.core.middleware;

import com.kmkbe.core.exception.IllegalApiKeyException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApiKeyAuthenticationFilterTest {

  @Test void attemptAuthenticationRequiresHeader() {
    var f = new ApiKeyAuthenticationFilter("/api/v1");
    var auth = mock(AuthenticationManager.class);
    f.setAuthenticationManager(auth);
    assertThatThrownBy(() -> f.attemptAuthentication(new MockHttpServletRequest(), new MockHttpServletResponse()))
      .isInstanceOf(IllegalApiKeyException.class);
    verifyNoInteractions(auth);
  }

  @Test void attemptAuthenticationUsesHeaderAndManager() throws Exception {
    var f = new ApiKeyAuthenticationFilter("/api/v1");
    var auth = mock(AuthenticationManager.class);
    Authentication expected = mock(Authentication.class);
    when(auth.authenticate(any())).thenReturn(expected);
    f.setAuthenticationManager(auth);
    var req = new MockHttpServletRequest();
    req.addHeader("ApiKey", "key-1");
    assertThat(f.attemptAuthentication(req, new MockHttpServletResponse())).isSameAs(expected);
    verify(auth).authenticate(any(com.kmkbe.core.service.ApiKeyAuthenticationService.class));
  }

  @Test void constructors() {
    assertThat(new ApiKeyAuthenticationFilter("/api/v1")).isNotNull();
    assertThat(new ApiKeyAuthenticationFilter((org.springframework.security.web.util.matcher.RequestMatcher) r -> true)).isNotNull();
    AuthenticationManager am = mock(AuthenticationManager.class);
    assertThat(new ApiKeyAuthenticationFilter("/api/v1", am)).isNotNull();
    assertThat(new ApiKeyAuthenticationFilter((org.springframework.security.web.util.matcher.RequestMatcher) r -> true, am)).isNotNull();
  }
}
