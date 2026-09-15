package com.kmkbe.support;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class DocumentControllerTestConfiguration {
  @Bean
  public static BeanFactoryPostProcessor documentControllerConstructorReferences() {
    return factory -> {
      // Test-only wiring matching the qualifiers already declared on production fields.
      var arguments = factory.getBeanDefinition("documentController").getConstructorArgumentValues();
      arguments.addIndexedArgumentValue(1, new RuntimeBeanReference("internalUserDetailService"));
      arguments.addIndexedArgumentValue(2, new RuntimeBeanReference("userDetailsService"));
    };
  }
}
