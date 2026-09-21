package com.kmkbe.support;

import com.kmkbe.modules.common.service.refresh_token.IRefreshTokenServices;
import com.kmkbe.modules.user.service.AuthInternalServices;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class AuthInternalServicesTestConfigurationTest {
  @Test
  void selectsDatabaseRefreshTokensWhenBothImplementationsExist() {
    var databaseTokens = mock(IRefreshTokenServices.class);
    var cacheTokens = mock(IRefreshTokenServices.class);
    try (var context = new AnnotationConfigApplicationContext()) {
      var parameters = AuthInternalServices.class.getDeclaredConstructors()[0].getParameterTypes();
      assertThat(parameters).hasSize(10);
      for (int index = 0; index < parameters.length; index++) {
        context.getBeanFactory().registerSingleton("dependency" + index, mock(parameters[index]));
      }
      context.getBeanFactory().registerSingleton("DbRefreshTokenServices", databaseTokens);
      context.getBeanFactory().registerSingleton("CacheRefreshTokenServices", cacheTokens);
      context.register(AuthInternalServices.class, AuthInternalServicesTestConfiguration.class);
      context.refresh();
      assertThat(ReflectionTestUtils.getField(context.getBean(AuthInternalServices.class),
          "refreshTokenServices")).isSameAs(databaseTokens);
      verifyNoInteractions(databaseTokens, cacheTokens);
    }
  }
}
