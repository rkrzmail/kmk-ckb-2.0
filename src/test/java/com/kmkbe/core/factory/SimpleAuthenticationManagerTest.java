package com.kmkbe.core.factory;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
class SimpleAuthenticationManagerTest {
  @Test void authenticateSetsAuthenticated() {
    var mgr = new SimpleAuthenticationManager();
    Authentication auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn("u");
    var result = mgr.authenticate(auth);
    verify(auth).setAuthenticated(true);
    assertThat(result).isSameAs(auth);
  }
}
