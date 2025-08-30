package com.arojas.jce_consulta_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

import com.arojas.jce_consulta_api.config.JceConfigurationProperties;

@SpringBootApplication
@EnableRetry
@EnableConfigurationProperties(JceConfigurationProperties.class)
@EnableFeignClients(basePackages = "com.arojas.jce_consulta_api.client")
public class JceConsultaMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JceConsultaMicroserviceApplication.class, args);
	}

}
