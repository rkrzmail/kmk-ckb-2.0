package com.kmkbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class KmkbeApplication {

	public static void main(String[] args) {
		SpringApplication.run(KmkbeApplication.class, args);
	}

}
