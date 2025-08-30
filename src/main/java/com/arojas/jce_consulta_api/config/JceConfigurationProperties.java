/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 *
 * @author arojas
 *         * Propiedades de configuración para el cliente JCE
 *
 */

@Data
@Validated
@ConfigurationProperties(prefix = "app.jce")
public class JceConfigurationProperties {

	@NotBlank(message = "La URL base de JCE es requerida")
	private String baseUrl;

	@NotBlank(message = "El Service ID de JCE es requerido")
	private String serviceId;

	@NotBlank(message = "El endpoint de JCE es requerido")
	private String endpoint;

	private Timeout timeout = new Timeout();
	private Retry retry = new Retry();

	@Data
	public static class Timeout {
		@Positive(message = "El timeout de conexión debe ser positivo")
		private int connect = 5000;

		@Positive(message = "El timeout de lectura debe ser positivo")
		private int read = 15000;
	}

	@Data
	public static class Retry {
		@Positive(message = "El número máximo de reintentos debe ser positivo")
		private int maxAttempts = 3;

		@Positive(message = "El delay de backoff debe ser positivo")
		private long backoffDelay = 1000;
	}
}