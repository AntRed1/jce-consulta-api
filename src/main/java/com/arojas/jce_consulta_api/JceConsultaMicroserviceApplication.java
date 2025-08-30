package com.arojas.jce_consulta_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class JceConsultaMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JceConsultaMicroserviceApplication.class, args);
	}

}
