package com.kmkbe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.kmkbe.support.DocumentControllerTestConfiguration;
import com.kmkbe.support.AuthInternalServicesTestConfiguration;

@SpringBootTest
@Import({DocumentControllerTestConfiguration.class, AuthInternalServicesTestConfiguration.class})
class KmkbeApplicationTests {

	@Test
	void contextLoads() {
	}

}
