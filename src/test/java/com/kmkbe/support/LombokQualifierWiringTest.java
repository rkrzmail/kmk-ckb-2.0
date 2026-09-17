package com.kmkbe.support;

import com.kmkbe.core.service.JwtService;
import com.kmkbe.modules.common.service.refresh_token.IRefreshTokenServices;
import com.kmkbe.modules.loan_submission.controller.DocumentController;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import com.kmkbe.modules.user.service.AuthInternalServices;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LombokQualifierWiringTest {
  @Test
  void documentControllerSelectsDistinctUserDetailsServicesWithoutTestConfiguration() {
    var internalUsers = mock(UserDetailsService.class);
    var customerUsers = mock(UserDetailsService.class);
    try (var context = new AnnotationConfigApplicationContext()) {
      context.getBeanFactory().registerSingleton("internalUserDetailService", internalUsers);
      context.getBeanFactory().registerSingleton("userDetailsService", customerUsers);
      context.getBeanFactory().registerSingleton("jwtService", mock(JwtService.class));
      context.getBeanFactory().registerSingleton("documentService", mock(DocumentService.class));
      context.register(DocumentController.class);
      context.refresh();

      var controller = context.getBean(DocumentController.class);
      assertThat(ReflectionTestUtils.getField(controller, "internalUserDetails")).isSameAs(internalUsers);
      assertThat(ReflectionTestUtils.getField(controller, "customerUserDetails")).isSameAs(customerUsers);
    }
  }

  @Test
  void authInternalServicesSelectsDatabaseRefreshTokensWithoutTestConfiguration() {
    var databaseTokens = mock(IRefreshTokenServices.class);
    var cacheTokens = mock(IRefreshTokenServices.class);
    try (var context = new AnnotationConfigApplicationContext()) {
      var parameters = AuthInternalServices.class.getDeclaredConstructors()[0].getParameterTypes();
      for (int index = 0; index < parameters.length - 1; index++) {
        context.getBeanFactory().registerSingleton("dependency" + index, mock(parameters[index]));
      }
      context.getBeanFactory().registerSingleton("DbRefreshTokenServices", databaseTokens);
      context.getBeanFactory().registerSingleton("CacheRefreshTokenServices", cacheTokens);
      context.register(AuthInternalServices.class);
      context.refresh();

      assertThat(ReflectionTestUtils.getField(context.getBean(AuthInternalServices.class),
          "refreshTokenServices")).isSameAs(databaseTokens);
    }
  }
}
