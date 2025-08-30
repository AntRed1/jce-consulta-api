/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.client;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.arojas.jce_consulta_api.config.JceConfigurationProperties;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author arojas
 *         * Configuración personalizada para el cliente Feign JCE
 *
 */

@Configuration
@RequiredArgsConstructor
@Slf4j
public class JceFeignClientConfiguration {

	private final JceConfigurationProperties jceProperties;

	@Bean
	public Request.Options requestOptions() {
		return new Request.Options(
				jceProperties.getTimeout().getConnect(),
				TimeUnit.MILLISECONDS,
				jceProperties.getTimeout().getRead(),
				TimeUnit.MILLISECONDS,
				true);
	}

	@Bean
	public Retryer retryer() {
		return new Retryer.Default(
				jceProperties.getRetry().getBackoffDelay(),
				TimeUnit.SECONDS.toMillis(5),
				jceProperties.getRetry().getMaxAttempts());
	}

	@Bean
	public Logger.Level feignLoggerLevel() {
		return Logger.Level.BASIC;
	}

	@Bean
	public ErrorDecoder errorDecoder() {
		return new JceErrorDecoder();
	}

	/**
	 * Decodificador de errores personalizado para JCE
	 */
	public static class JceErrorDecoder implements ErrorDecoder {
		private final ErrorDecoder defaultErrorDecoder = new Default();

		@Override
		public Exception decode(String methodKey, feign.Response response) {
			log.error("Error en llamada a JCE. Método: {}, Status: {}, Reason: {}",
					methodKey, response.status(), response.reason());

			return switch (response.status()) {
				case 400 -> new JceClientException("Parámetros inválidos en la consulta JCE");
				case 404 -> new JceClientException("Servicio JCE no encontrado");
				case 500 -> new JceClientException("Error interno del servidor JCE");
				case 503 -> new JceClientException("Servicio JCE temporalmente no disponible");
				default -> defaultErrorDecoder.decode(methodKey, response);
			};
		}
	}

	/**
	 * Excepción personalizada para errores del cliente JCE
	 */
	public static class JceClientException extends RuntimeException {
		public JceClientException(String message) {
			super(message);
		}

		public JceClientException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}