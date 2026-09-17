package com.kmkbe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import com.kmkbe.support.DocumentControllerTestConfiguration;
import com.kmkbe.support.AuthInternalServicesTestConfiguration;

@SpringBootTest
@Import({DocumentControllerTestConfiguration.class, AuthInternalServicesTestConfiguration.class,
    KmkbeApplicationTests.ConstructorWiringTestConfiguration.class})
class KmkbeApplicationTests {

	@TestConfiguration(proxyBeanMethods = false)
	static class ConstructorWiringTestConfiguration {
		@Bean
		static BeanFactoryPostProcessor constructorReferences() {
			return factory -> {
				var filterArguments = factory.getBeanDefinition("jwtAuthenticationFilter")
					.getConstructorArgumentValues();
				filterArguments.addIndexedArgumentValue(3, new RuntimeBeanReference("userDetailsService"));
				filterArguments.addIndexedArgumentValue(5, new RuntimeBeanReference("internalUserDetailService"));
				factory.getBeanDefinition("authService").getConstructorArgumentValues()
					.addIndexedArgumentValue(17, new RuntimeBeanReference("DbRefreshTokenServices"));
			};
		}
	}

	@Test
	void contextLoads() {
	}

}
