package com.kmkbe.modules.customer.service;

import com.kmkbe.modules.common.service.refresh_token.IRefreshTokenServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class AuthServiceWiringTest {
  @Test
  void declaresDatabaseRefreshTokenQualifierOnField() throws NoSuchFieldException {
    var field = AuthService.class.getDeclaredField("refreshTokenServices");
    assertThat(field.getAnnotation(Qualifier.class).value()).isEqualTo("DbRefreshTokenServices");
  }

  @Test
  void injectsDatabaseRefreshTokensWhenItIsTheOnlyCandidate() {
    var databaseTokens = mock(IRefreshTokenServices.class);
    try (var context = new AnnotationConfigApplicationContext()) {
      context.getBeanFactory().registerSingleton("DbRefreshTokenServices", databaseTokens);
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
      verifyNoInteractions(databaseTokens);
    }
  }
}
