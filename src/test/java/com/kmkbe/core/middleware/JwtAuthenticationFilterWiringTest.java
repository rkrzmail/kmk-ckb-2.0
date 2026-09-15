package com.kmkbe.core.middleware;

import com.kmkbe.core.domain.repository.RedisRepository;
import com.kmkbe.core.service.JwtLoanSubmissionService;
import com.kmkbe.core.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JwtAuthenticationFilterWiringTest {
  @Test
  void selectsCorrectNamedDependenciesWhenMultipleCandidatesExist() {
    var customerUsers = mock(UserDetailsService.class);
    var internalUsers = mock(UserDetailsService.class);
    var resolver = mock(HandlerExceptionResolver.class);
    try (var context = new AnnotationConfigApplicationContext()) {
      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
        "spring.profiles.active=test", "security.api.key=test-key", "env=test");
      context.registerBean("userDetailsService", UserDetailsService.class, () -> customerUsers);
      context.registerBean("internalUserDetailService", UserDetailsService.class, () -> internalUsers);
      context.registerBean("handlerExceptionResolver", HandlerExceptionResolver.class, () -> resolver);
      context.registerBean("otherResolver", HandlerExceptionResolver.class, () -> mock(HandlerExceptionResolver.class));
      context.getBeanFactory().registerSingleton("jwtService", mock(JwtService.class));
      context.getBeanFactory().registerSingleton("jwtLoanSubmissionService", mock(JwtLoanSubmissionService.class));
      context.getBeanFactory().registerSingleton("redisRepository", mock(RedisRepository.class));
      context.register(JwtAuthenticationFilter.class);
      context.refresh();
      var filter = context.getBean(JwtAuthenticationFilter.class);
      assertThat(ReflectionTestUtils.getField(filter, "userDetailsService")).isSameAs(customerUsers);
      assertThat(ReflectionTestUtils.getField(filter, "internalUserDetailsService")).isSameAs(internalUsers);
      assertThat(ReflectionTestUtils.getField(filter, "handlerExceptionResolver")).isSameAs(resolver);
    }
  }
}
