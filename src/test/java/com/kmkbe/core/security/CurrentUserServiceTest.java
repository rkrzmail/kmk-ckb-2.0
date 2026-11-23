package com.kmkbe.core.security;

import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.user.entity.MstUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.SignatureException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrentUserServiceTest {
  private final CurrentUserService svc = new CurrentUserService();

  @AfterEach void clear() { SecurityContextHolder.clearContext(); }

  @Test void internalUserReturnsMstUser() throws Exception {
    MstUser user = mock(MstUser.class);
    when(user.getUsername()).thenReturn("alice");
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
    assertThat(svc.internalUser()).isSameAs(user);
    assertThat(svc.internalUsername()).isEqualTo("alice");
    svc.authenticatedInternalUser();
  }

  @Test void internalUsernameFallsBackToSystem() throws Exception {
    MstUser user = mock(MstUser.class);
    when(user.getUsername()).thenReturn(" ");
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
    assertThat(svc.internalUsername()).isEqualTo("SYSTEM");
  }

  @Test void internalUserThrowsWhenNotMstUser() {
    Customer c = mock(Customer.class);
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(c, null, List.of()));
    assertThatThrownBy(svc::internalUser).isInstanceOf(CommonInvalidException.class);
  }

  @Test void customerReturnsCustomer() throws Exception {
    Customer c = mock(Customer.class);
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(c, null, List.of()));
    assertThat(svc.customer()).isSameAs(c);
    assertThat(svc.customerOrNull()).isSameAs(c);
  }

  @Test void customerThrowsWhenNotCustomer() {
    MstUser u = mock(MstUser.class);
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(u, null, List.of()));
    assertThatThrownBy(svc::customer).isInstanceOf(SignatureException.class);
  }

  @Test void customerOrNullReturnsNullWhenAnonymousOrMissing() throws Exception {
    SecurityContextHolder.clearContext();
    assertThat(svc.customerOrNull()).isNull();
    var anon = new AnonymousAuthenticationToken("k", "anon", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(anon);
    assertThat(svc.customerOrNull()).isNull();
  }

  @Test void authenticationThrowsWhenMissingOrAnonymous() {
    SecurityContextHolder.clearContext();
    assertThatThrownBy(svc::internalUser).isInstanceOf(SignatureException.class);
    var anon = new AnonymousAuthenticationToken("k", "anon", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(anon);
    assertThatThrownBy(svc::internalUser).isInstanceOf(SignatureException.class);
  }

  @Test void usernameOrDefault() {
    SecurityContextHolder.clearContext();
    assertThat(svc.usernameOrDefault("def")).isEqualTo("def");
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("bob", null, List.of()));
    assertThat(svc.usernameOrDefault("def")).isEqualTo("bob");
  }
}
