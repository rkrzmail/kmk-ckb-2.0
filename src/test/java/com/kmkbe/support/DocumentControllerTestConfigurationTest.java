package com.kmkbe.support;

import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.loan_submission.controller.DocumentController;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DocumentControllerTestConfigurationTest {
  @Test
  void wiresRealControllerSecurely() {
    var documentService = mock(DocumentService.class);
    var currentUserService = mock(CurrentUserService.class);
    try (var context = new AnnotationConfigApplicationContext()) {
      context.getBeanFactory().registerSingleton("documentService", documentService);
      context.getBeanFactory().registerSingleton("currentUserService", currentUserService);
      context.register(DocumentControllerTestConfiguration.class, DocumentController.class);
      context.refresh();
      var controller = context.getBean(DocumentController.class);
      assertThat(ReflectionTestUtils.getField(controller, "documentService")).isSameAs(documentService);
      verifyNoInteractions(documentService);
    }
  }
}
