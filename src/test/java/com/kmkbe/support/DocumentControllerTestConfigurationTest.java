package com.kmkbe.support;

import com.kmkbe.core.service.JwtService;
import com.kmkbe.modules.loan_submission.controller.DocumentController;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DocumentControllerTestConfigurationTest {
  @Test
  void wiresRealControllerToDistinctCustomerAndInternalServices() {
    var internalUsers = mock(UserDetailsService.class);
    var customerUsers = mock(UserDetailsService.class);
    try (var context = new AnnotationConfigApplicationContext()) {
      context.getBeanFactory().registerSingleton("internalUserDetailService", internalUsers);
      context.getBeanFactory().registerSingleton("userDetailsService", customerUsers);
      context.getBeanFactory().registerSingleton("jwtService", mock(JwtService.class));
      context.getBeanFactory().registerSingleton("documentService", mock(DocumentService.class));
      context.register(DocumentControllerTestConfiguration.class, DocumentController.class);
      context.refresh();
      var controller = context.getBean(DocumentController.class);
      assertThat(ReflectionTestUtils.getField(controller, "internalUserDetails")).isSameAs(internalUsers);
      assertThat(ReflectionTestUtils.getField(controller, "customerUserDetails")).isSameAs(customerUsers);
      verifyNoInteractions(internalUsers, customerUsers);
    }
  }
}
