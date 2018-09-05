package com.yossale.restcrudspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RestCrudSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestCrudSpringApplication.class, args);
	}
}
