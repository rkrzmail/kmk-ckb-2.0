package com.kmkbe.modules.customer.service;

import com.kmkbe.modules.common.service.refresh_token.IRefreshTokenServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthServiceWiringTest {
  @Test
  void explicitlyQualifiesDatabaseRefreshTokenConstructorParameter() {
    var parameter = java.util.Arrays.stream(AuthService.class.getConstructors()[0].getParameters())
      .filter(p -> p.getType().equals(IRefreshTokenServices.class)).findFirst().orElseThrow();
    assertThat(parameter.getAnnotation(Qualifier.class)).isNotNull();
    assertThat(parameter.getAnnotation(Qualifier.class).value()).isEqualTo("DbRefreshTokenServices");
  }

  @Test
  void selectsDatabaseRefreshTokensWhenCacheBeanAlsoExists() {
    var databaseTokens = mock(IRefreshTokenServices.class);
    var cacheTokens = mock(IRefreshTokenServices.class);
    try (var context = new AnnotationConfigApplicationContext()) {
      context.getBeanFactory().registerSingleton("DbRefreshTokenServices", databaseTokens);
      context.getBeanFactory().registerSingleton("CacheRefreshTokenServices", cacheTokens);
      int index = 0;
      for (Class<?> type : AuthService.class.getConstructors()[0].getParameterTypes()) {
        if (!type.equals(IRefreshTokenServices.class)) {
          context.getBeanFactory().registerSingleton("dependency" + index++, mock(type));
        }
      }
      context.register(AuthService.class);
      context.refresh();
      assertThat(ReflectionTestUtils.getField(context.getBean(AuthService.class), "refreshTokenServices"))
        .isSameAs(databaseTokens);
      verifyNoInteractions(databaseTokens, cacheTokens);
    }
  }
}
