package com.kmkbe.support;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class AuthInternalServicesTestConfiguration {
  @Bean
  public static BeanFactoryPostProcessor authInternalServicesConstructorReferences() {
    return factory -> {
      // Match the production field qualifier without changing production constructors.
      var arguments = factory.getBeanDefinition("authInternalServices").getConstructorArgumentValues();
      arguments.addIndexedArgumentValue(10, new RuntimeBeanReference("DbRefreshTokenServices"));
    };
  }
}
