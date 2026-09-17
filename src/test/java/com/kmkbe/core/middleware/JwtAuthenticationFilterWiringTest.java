package com.kmkbe.core.middleware;

import com.kmkbe.core.domain.repository.RedisRepository;
import com.kmkbe.core.service.JwtLoanSubmissionService;
import com.kmkbe.core.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtAuthenticationFilterWiringTest {
  @Test
  void userDetailsFieldsDeclareExpectedQualifiers() throws NoSuchFieldException {
    assertThat(JwtAuthenticationFilter.class.getDeclaredField("userDetailsService")
        .getAnnotation(Qualifier.class).value()).isEqualTo("userDetailsService");
    assertThat(JwtAuthenticationFilter.class.getDeclaredField("internalUserDetailsService")
        .getAnnotation(Qualifier.class).value()).isEqualTo("internalUserDetailService");
  }

  @Test
  void constructorAcceptsDistinctUserDetailsServicesAndResolver() {
    var customerUsers = mock(UserDetailsService.class);
    var internalUsers = mock(UserDetailsService.class);
    var resolver = mock(HandlerExceptionResolver.class);
    var filter = new JwtAuthenticationFilter(resolver, mock(JwtService.class),
        mock(JwtLoanSubmissionService.class), customerUsers, mock(RedisRepository.class), internalUsers);
    assertThat(ReflectionTestUtils.getField(filter, "userDetailsService")).isSameAs(customerUsers);
    assertThat(ReflectionTestUtils.getField(filter, "internalUserDetailsService")).isSameAs(internalUsers);
    assertThat(ReflectionTestUtils.getField(filter, "handlerExceptionResolver")).isSameAs(resolver);
  }
}
