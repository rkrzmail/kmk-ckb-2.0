package com.kmkbe.support;

import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.common.service.refresh_token.IRefreshTokenServices;
import com.kmkbe.modules.loan_submission.controller.DocumentController;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import com.kmkbe.modules.user.service.AuthInternalServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LombokQualifierWiringTest {
  @Test
  void documentControllerWiresSecurelyWithoutTokenBypassFields() throws NoSuchFieldException {
    // C10: DocumentController tidak lagi menyimpan JwtService / UserDetailsService
    // untuk validasi token via query param. Auth ditangani filter + CurrentUserService di service layer.
    var documentService = mock(DocumentService.class);
    var currentUserService = mock(CurrentUserService.class);
    assertThat(DocumentController.class.getDeclaredFields())
        .extracting(f -> f.getName())
        .doesNotContain("jwtService", "internalUserDetails", "customerUserDetails", "token");
    try (var context = new AnnotationConfigApplicationContext()) {
      context.getBeanFactory().registerSingleton("documentService", documentService);
      context.getBeanFactory().registerSingleton("currentUserService", currentUserService);
      context.register(DocumentController.class);
      context.refresh();
      var controller = context.getBean(DocumentController.class);
      assertThat(ReflectionTestUtils.getField(controller, "documentService")).isSameAs(documentService);
    }
  }

  @Test
  void authInternalServicesSelectsDatabaseRefreshTokensWhenCacheBeanAlsoExists() throws NoSuchFieldException {
    var databaseTokens = mock(IRefreshTokenServices.class);
    var cacheTokens = mock(IRefreshTokenServices.class);
    try (var context = new AnnotationConfigApplicationContext()) {
      var parameters = AuthInternalServices.class.getDeclaredConstructors()[0].getParameterTypes();
      assertThat(AuthInternalServices.class.getDeclaredField("refreshTokenServices")
          .getAnnotation(Qualifier.class).value()).isEqualTo("DbRefreshTokenServices");
      for (int index = 0; index < parameters.length; index++) {
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
