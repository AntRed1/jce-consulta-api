/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.arojas.jce_consulta_api.client.JceFeignClient;
import com.arojas.jce_consulta_api.client.JceFeignClientConfiguration.JceClientException;
import com.arojas.jce_consulta_api.config.JceConfigurationProperties;
import com.arojas.jce_consulta_api.dto.CedulaResultDto;
import com.arojas.jce_consulta_api.mapper.JceResponseMapper;
import com.arojas.jce_consulta_api.util.CedulaValidationUtils;
import com.arojas.jce_consulta_api.util.CedulaValidationUtils.CedulaInfo;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import com.arojas.jce_consulta_api.util.CedulaValidationUtils.XmlToJsonResult;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Cliente mejorado para consultas JCE usando OpenFeign
 * Actualizado para usar CedulaValidationUtils unificada y conversión XML a JSON
 * Maneja excepciones específicas y genéricas
 * Implementa circuit breaker, retry y caché
 * Proporciona método para verificar estado del servicio JCE
 * Incluye DTO para información de configuración del cliente
 *
 * @author arojas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JceClient {

	private final JceFeignClient jceFeignClient;
	private final JceConfigurationProperties jceProperties;
	private final CedulaValidationUtils cedulaValidationUtils;
	private final JceResponseMapper responseMapper;

	private static final String JCE_CIRCUIT_BREAKER = "jceService";

	/**
	 * Realiza consulta de cédula con circuit breaker, retry y caché
	 * Retorna respuesta en formato JSON
	 */
	@Cacheable(value = "cedulaQueries", key = "#cedula", unless = "#result == null or !#result.success")
	@CircuitBreaker(name = JCE_CIRCUIT_BREAKER, fallbackMethod = "fallbackQueryCedula")
	@TimeLimiter(name = JCE_CIRCUIT_BREAKER)
	@Retryable(retryFor = {
			JceClientException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
	public CompletableFuture<CedulaResultDto> queryCedulaAsync(String cedula) {
		return CompletableFuture.supplyAsync(() -> queryCedula(cedula));
	}

	/**
	 * Realiza consulta de cédula síncrona y retorna resultado con JSON
	 */
	public CedulaResultDto queryCedula(String cedula) {
		log.info("Iniciando consulta JCE para cédula: {}", cedulaValidationUtils.maskCedula(cedula));

		try {
			// Validar y procesar cédula
			CedulaInfo cedulaInfo = CedulaInfo.from(cedula, cedulaValidationUtils);

			if (!cedulaInfo.digitoVerificadorValido()) {
				log.warn("Dígito verificador inválido para cédula: {}",
						cedulaValidationUtils.maskCedula(cedula));
			}

			// Realizar consulta al servicio JCE
			String xmlResponse = consultarJceService(cedulaInfo);

			// Procesar respuesta XML y convertir a JSON
			XmlToJsonResult xmlToJsonResult = cedulaValidationUtils.processJceXmlResponse(xmlResponse);

			if (!xmlToJsonResult.success() || xmlToJsonResult.parsedData() == null
					|| !xmlToJsonResult.parsedData().hasValidData()) {
				log.warn("No se encontraron datos válidos para cédula: {}",
						cedulaValidationUtils.maskCedula(cedula));
				return createEmptyResult(cedulaInfo,
						"No se encontraron datos para la cédula especificada", null);
			}

			// Mapear a DTO de resultado
			CedulaResultDto result = responseMapper.toResultDto(xmlToJsonResult.parsedData(), cedulaInfo);
			result.setSuccess(true);
			result.setMessage("Consulta realizada exitosamente");
			result.setQueryTimestamp(LocalDateTime.now());

			// Agregar la respuesta JSON al resultado
			result.setJsonResponse(xmlToJsonResult.jsonResponse());

			log.info("Consulta JCE completada exitosamente para cédula: {}",
					cedulaValidationUtils.maskCedula(cedula));
			return result;

		} catch (JceClientException e) {
			log.error("Error específico del cliente JCE para cédula {}: {}",
					cedulaValidationUtils.maskCedula(cedula), e.getMessage());
			throw e;

		} catch (Exception e) {
			log.error("Error inesperado consultando cédula {} en JCE: {}",
					cedulaValidationUtils.maskCedula(cedula), e.getMessage(), e);
			throw new JceClientException("Error consultando cédula en JCE: " + e.getMessage(), e);
		}
	}

	/**
	 * Consulta cédula y retorna solo el JSON de respuesta
	 * 
	 * @param cedula Cédula a consultar
	 * @return JSON string con los datos de la cédula
	 */
	public String queryCedulaAsJson(String cedula) {
		log.info("Consultando cédula como JSON: {}", cedulaValidationUtils.maskCedula(cedula));

		try {
			CedulaInfo cedulaInfo = CedulaInfo.from(cedula, cedulaValidationUtils);
			String xmlResponse = consultarJceService(cedulaInfo);

			XmlToJsonResult result = cedulaValidationUtils.processJceXmlResponse(xmlResponse);

			if (!result.success()) {
				log.warn("Error procesando respuesta XML para cédula {}: {}",
						cedulaValidationUtils.maskCedula(cedula), result.errorMessage());
				return "{}";
			}

			return result.jsonResponse();

		} catch (Exception e) {
			log.error("Error consultando cédula {} como JSON: {}",
					cedulaValidationUtils.maskCedula(cedula), e.getMessage(), e);
			return "{}";
		}
	}

	/**
	 * Verifica el estado del servicio JCE
	 */
	@CircuitBreaker(name = JCE_CIRCUIT_BREAKER, fallbackMethod = "fallbackHealthCheck")
	public boolean checkJceServiceHealth() {
		log.info("Verificando estado del servicio JCE");

		try {
			// Usar cédula de prueba ficticia
			String testCedula = "00100000001";
			CedulaInfo testCedulaInfo = CedulaInfo.from(testCedula, cedulaValidationUtils);

			String response = consultarJceService(testCedulaInfo);

			// Verificar que la respuesta no esté vacía
			boolean healthy = response != null && !response.trim().isEmpty();

			log.info("Verificación de salud JCE completada. Estado: {}", healthy ? "UP" : "DOWN");
			return healthy;

		} catch (Exception e) {
			log.error("Servicio JCE no disponible: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Obtiene información de configuración del cliente JCE
	 */
	public JceClientInfo getClientInfo() {
		return JceClientInfo.builder()
				.baseUrl(jceProperties.getBaseUrl())
				.serviceId(jceProperties.getServiceId())
				.endpoint(jceProperties.getEndpoint())
				.connectTimeout(jceProperties.getTimeout().getConnect())
				.readTimeout(jceProperties.getTimeout().getRead())
				.maxRetryAttempts(jceProperties.getRetry().getMaxAttempts())
				.retryBackoffDelay(jceProperties.getRetry().getBackoffDelay())
				.serviceHealthy(checkJceServiceHealth())
				.configurationValid(isConfigurationValid())
				.build();
	}

	// ================= MÉTODOS PRIVADOS =================

	private String consultarJceService(CedulaInfo cedulaInfo) {
		log.debug("Consultando servicio JCE con parámetros - Municipio: {}, Secuencia: {}, Verificador: {}",
				cedulaInfo.municipio(), "****", cedulaInfo.digitoVerificador());

		try {
			return jceFeignClient.consultarCedula(
					jceProperties.getServiceId(),
					cedulaInfo.municipio(),
					cedulaInfo.secuencia(),
					cedulaInfo.digitoVerificador());
		} catch (Exception e) {
			log.error("Error en llamada al servicio JCE: {}", e.getMessage());
			throw new JceClientException("Error comunicándose con el servicio JCE", e);
		}
	}

	private CedulaResultDto createEmptyResult(CedulaInfo cedulaInfo, String message, String jsonResponse) {
		return CedulaResultDto.builder()
				.cedula(cedulaInfo.cedulaFormateada())
				.success(false)
				.message(message)
				.queryTimestamp(LocalDateTime.now())
				.jsonResponse(jsonResponse != null ? jsonResponse : "{}")
				.build();
	}

	private boolean isConfigurationValid() {
		return jceProperties.getBaseUrl() != null &&
				!jceProperties.getBaseUrl().trim().isEmpty() &&
				jceProperties.getServiceId() != null &&
				!jceProperties.getServiceId().trim().isEmpty() &&
				jceProperties.getEndpoint() != null &&
				!jceProperties.getEndpoint().trim().isEmpty();
	}

	// ================= MÉTODOS FALLBACK =================

	public CompletableFuture<CedulaResultDto> fallbackQueryCedula(String cedula, Exception ex) {
		log.warn("Fallback activado para consulta de cédula {}: {}",
				cedulaValidationUtils.maskCedula(cedula), ex.getMessage());

		CedulaInfo cedulaInfo = CedulaInfo.from(cedula, cedulaValidationUtils);
		CedulaResultDto fallbackResult = createEmptyResult(cedulaInfo,
				"Servicio JCE temporalmente no disponible. Intente más tarde.", "{}");

		return CompletableFuture.completedFuture(fallbackResult);
	}

	public boolean fallbackHealthCheck(Exception ex) {
		log.warn("Fallback activado para verificación de salud JCE: {}", ex.getMessage());
		return false;
	}

	// ================= DTO PARA INFORMACIÓN DEL CLIENTE =================

	public record JceClientInfo(
			String baseUrl,
			String serviceId,
			String endpoint,
			int connectTimeout,
			int readTimeout,
			int maxRetryAttempts,
			long retryBackoffDelay,
			boolean serviceHealthy,
			boolean configurationValid) {

		public static JceClientInfoBuilder builder() {
			return new JceClientInfoBuilder();
		}
	}

	// Builder para JceClientInfo
	public static class JceClientInfoBuilder {
		private String baseUrl;
		private String serviceId;
		private String endpoint;
		private int connectTimeout;
		private int readTimeout;
		private int maxRetryAttempts;
		private long retryBackoffDelay;
		private boolean serviceHealthy;
		private boolean configurationValid;

		public JceClientInfoBuilder baseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
			return this;
		}

		public JceClientInfoBuilder serviceId(String serviceId) {
			this.serviceId = serviceId;
			return this;
		}

		public JceClientInfoBuilder endpoint(String endpoint) {
			this.endpoint = endpoint;
			return this;
		}

		public JceClientInfoBuilder connectTimeout(int connectTimeout) {
			this.connectTimeout = connectTimeout;
			return this;
		}

		public JceClientInfoBuilder readTimeout(int readTimeout) {
			this.readTimeout = readTimeout;
			return this;
		}

		public JceClientInfoBuilder maxRetryAttempts(int maxRetryAttempts) {
			this.maxRetryAttempts = maxRetryAttempts;
			return this;
		}

		public JceClientInfoBuilder retryBackoffDelay(long retryBackoffDelay) {
			this.retryBackoffDelay = retryBackoffDelay;
			return this;
		}

		public JceClientInfoBuilder serviceHealthy(boolean serviceHealthy) {
			this.serviceHealthy = serviceHealthy;
			return this;
		}

		public JceClientInfoBuilder configurationValid(boolean configurationValid) {
			this.configurationValid = configurationValid;
			return this;
		}

		public JceClientInfo build() {
			return new JceClientInfo(baseUrl, serviceId, endpoint, connectTimeout, readTimeout,
					maxRetryAttempts, retryBackoffDelay, serviceHealthy, configurationValid);
		}
	}
}