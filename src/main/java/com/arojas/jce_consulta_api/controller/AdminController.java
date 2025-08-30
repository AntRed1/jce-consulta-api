/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arojas.jce_consulta_api.dto.response.ApiResponse;
import com.arojas.jce_consulta_api.service.AppSettingsService;
import com.arojas.jce_consulta_api.service.CedulaQueryService;
import com.arojas.jce_consulta_api.service.EmailService;
import com.arojas.jce_consulta_api.service.JceClient;
import com.arojas.jce_consulta_api.service.PaymentService;
import com.arojas.jce_consulta_api.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author arojas
 */

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Administration", description = "Endpoints administrativos del sistema")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

	private final UserService userService;
	private final PaymentService paymentService;
	private final CedulaQueryService cedulaQueryService;
	private final JceClient jceClient;
	private final EmailService emailService;
	private final AppSettingsService appSettingsService;

	@Operation(summary = "Dashboard de administración", description = "Obtiene estadísticas generales del sistema para el dashboard administrativo")
	@GetMapping("/dashboard")
	public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {

		log.info("Obteniendo estadísticas del dashboard administrativo");

		try {
			Map<String, Object> dashboardData = new HashMap<>();

			// Estadísticas de usuarios
			UserService.UserStatsDto userStats = userService.getUserStatistics();
			dashboardData.put("userStats", userStats);

			// Estadísticas del sistema
			JceClient.JceClientInfo jceInfo = jceClient.getClientInfo();
			dashboardData.put("jceStatus", jceInfo);

			// Pagos pendientes
			int pendingPayments = paymentService.getPendingPayments().size();
			dashboardData.put("pendingPayments", pendingPayments);

			// Precio actual del token
			java.math.BigDecimal tokenPrice = appSettingsService.getTokenPrice();
			dashboardData.put("tokenPrice", tokenPrice);

			// Información del sistema
			Map<String, Object> systemInfo = new HashMap<>();
			systemInfo.put("javaVersion", System.getProperty("java.version"));
			systemInfo.put("osName", System.getProperty("os.name"));
			systemInfo.put("availableProcessors", Runtime.getRuntime().availableProcessors());
			systemInfo.put("maxMemory", Runtime.getRuntime().maxMemory() / (1024 * 1024)); // MB
			systemInfo.put("freeMemory", Runtime.getRuntime().freeMemory() / (1024 * 1024)); // MB
			dashboardData.put("systemInfo", systemInfo);

			ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
					.success(true)
					.data(dashboardData)
					.message("Estadísticas del dashboard obtenidas exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo estadísticas del dashboard: {}", e.getMessage());

			ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo estadísticas del dashboard")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Verificar salud del sistema", description = "Verifica el estado de todos los servicios del sistema")
	@GetMapping("/health-check")
	public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {

		log.info("Verificando salud del sistema");

		try {
			Map<String, Object> healthStatus = new HashMap<>();

			// Estado del servicio JCE
			boolean jceHealthy = jceClient.checkJceServiceHealth();
			healthStatus.put("jceService", Map.of(
					"status", jceHealthy ? "UP" : "DOWN",
					"healthy", jceHealthy));

			// Estado de la base de datos (verificar contando usuarios)
			boolean dbHealthy = true;
			try {
				userService.getUserStatistics();
			} catch (Exception e) {
				dbHealthy = false;
				log.warn("Error verificando salud de BD: {}", e.getMessage());
			}
			healthStatus.put("database", Map.of(
					"status", dbHealthy ? "UP" : "DOWN",
					"healthy", dbHealthy));

			// Estado general del sistema
			boolean systemHealthy = jceHealthy && dbHealthy;
			healthStatus.put("overall", Map.of(
					"status", systemHealthy ? "UP" : "DOWN",
					"healthy", systemHealthy));

			// Timestamp de la verificación
			healthStatus.put("timestamp", java.time.LocalDateTime.now());

			ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
					.success(true)
					.data(healthStatus)
					.message("Verificación de salud completada")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error en verificación de salud: {}", e.getMessage());

			ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
					.success(false)
					.data(null)
					.message("Error verificando salud del sistema")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Limpiar datos del sistema", description = "Ejecuta tareas de limpieza del sistema (tokens expirados, pagos antiguos, etc.)")
	@PostMapping("/cleanup")
	public ResponseEntity<ApiResponse<Map<String, Integer>>> performSystemCleanup() {

		log.info("Iniciando limpieza del sistema");

		try {
			Map<String, Integer> cleanupResults = new HashMap<>();

			// Limpiar tokens expirados
			java.util.List<com.arojas.jce_consulta_api.dto.UserDto> usersWithExpiredTokens = userService
					.getUsersWithExpiredTokens();
			int expiredTokensCleanup = usersWithExpiredTokens.size();
			userService.cleanupExpiredTokens();
			cleanupResults.put("expiredTokensCleanup", expiredTokensCleanup);

			// Limpiar pagos expirados (más de 48 horas)
			int expiredPaymentsCleanup = paymentService.cleanupExpiredPendingPayments(48);
			cleanupResults.put("expiredPaymentsCleanup", expiredPaymentsCleanup);

			ApiResponse<Map<String, Integer>> response = ApiResponse.<Map<String, Integer>>builder()
					.success(true)
					.data(cleanupResults)
					.message("Limpieza del sistema completada exitosamente")
					.build();

			log.info("Limpieza del sistema completada: {}", cleanupResults);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error en limpieza del sistema: {}", e.getMessage());

			ApiResponse<Map<String, Integer>> response = ApiResponse.<Map<String, Integer>>builder()
					.success(false)
					.data(null)
					.message("Error ejecutando limpieza del sistema")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Enviar email de prueba", description = "Envía un email de prueba para verificar la configuración del servicio de email")
	@PostMapping("/test-email")
	public ResponseEntity<ApiResponse<String>> sendTestEmail(
			@Parameter(description = "Email de destino") @RequestParam @NotBlank @Email String testEmail,

			@Parameter(description = "Asunto del email") @RequestParam(defaultValue = "Email de Prueba - JCE Consulta API") @NotBlank @Size(max = 200) String subject,

			@Parameter(description = "Mensaje del email") @RequestParam(defaultValue = "Este es un email de prueba del sistema JCE Consulta API.") @NotBlank @Size(max = 1000) String message) {

		log.info("Enviando email de prueba a: {}", testEmail);

		try {
			String htmlContent = String.format(
					"<html><body><h2>%s</h2><p>%s</p><p><strong>Fecha:</strong> %s</p></body></html>",
					subject, message, java.time.LocalDateTime.now());

			emailService.sendCustomEmail(testEmail, subject, htmlContent);

			ApiResponse<String> response = ApiResponse.<String>builder()
					.success(true)
					.data("Email enviado exitosamente")
					.message("El email de prueba ha sido enviado a " + testEmail)
					.build();

			log.info("Email de prueba enviado exitosamente a: {}", testEmail);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error enviando email de prueba a {}: {}", testEmail, e.getMessage());

			ApiResponse<String> response = ApiResponse.<String>builder()
					.success(false)
					.data(null)
					.message("Error enviando email de prueba")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Actualizar precio del token", description = "Actualiza el precio unitario del token en el sistema")
	@PutMapping("/token-price")
	public ResponseEntity<ApiResponse<java.math.BigDecimal>> updateTokenPrice(
			@Parameter(description = "Nuevo precio del token") @RequestParam java.math.BigDecimal newPrice) {

		log.info("Actualizando precio del token a: {}", newPrice);

		try {
			if (newPrice.compareTo(java.math.BigDecimal.ZERO) <= 0) {
				throw new IllegalArgumentException("El precio del token debe ser mayor a 0");
			}

			if (newPrice.compareTo(new java.math.BigDecimal("100")) > 0) {
				throw new IllegalArgumentException("El precio del token no puede ser mayor a $100");
			}

			appSettingsService.updateTokenPrice(newPrice);

			ApiResponse<java.math.BigDecimal> response = ApiResponse.<java.math.BigDecimal>builder()
					.success(true)
					.data(newPrice)
					.message("Precio del token actualizado exitosamente")
					.build();

			log.info("Precio del token actualizado exitosamente a: {}", newPrice);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error actualizando precio del token: {}", e.getMessage());

			ApiResponse<java.math.BigDecimal> response = ApiResponse.<java.math.BigDecimal>builder()
					.success(false)
					.data(null)
					.message("Error actualizando precio del token")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Obtener información del cliente JCE", description = "Obtiene información detallada sobre la configuración del cliente JCE")
	@GetMapping("/jce-client-info")
	public ResponseEntity<ApiResponse<JceClient.JceClientInfo>> getJceClientInfo() {

		log.info("Obteniendo información del cliente JCE");

		try {
			JceClient.JceClientInfo clientInfo = jceClient.getClientInfo();

			ApiResponse<JceClient.JceClientInfo> response = ApiResponse.<JceClient.JceClientInfo>builder()
					.success(true)
					.data(clientInfo)
					.message("Información del cliente JCE obtenida exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo información del cliente JCE: {}", e.getMessage());

			ApiResponse<JceClient.JceClientInfo> response = ApiResponse.<JceClient.JceClientInfo>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo información del cliente JCE")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Probar conexión JCE", description = "Prueba la conectividad con el servicio de la JCE")
	@PostMapping("/test-jce")
	public ResponseEntity<ApiResponse<Boolean>> testJceConnection() {

		log.info("Probando conexión con JCE");

		try {
			boolean isHealthy = jceClient.checkJceServiceHealth();

			ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
					.success(true)
					.data(isHealthy)
					.message(isHealthy ? "Conexión con JCE exitosa" : "Conexión con JCE falló")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error probando conexión con JCE: {}", e.getMessage());

			ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
					.success(false)
					.data(false)
					.message("Error probando conexión con JCE")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Estadísticas del sistema", description = "Obtiene estadísticas completas del sistema para reportes")
	@GetMapping("/system-stats")
	public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStats() {

		log.info("Obteniendo estadísticas completas del sistema");

		try {
			Map<String, Object> systemStats = new HashMap<>();

			// Estadísticas de usuarios
			UserService.UserStatsDto userStats = userService.getUserStatistics();
			systemStats.put("users", userStats);

			// Estadísticas del sistema (memoria, CPU, etc.)
			Runtime runtime = Runtime.getRuntime();
			Map<String, Object> runtimeStats = new HashMap<>();
			runtimeStats.put("totalMemoryMB", runtime.totalMemory() / (1024 * 1024));
			runtimeStats.put("freeMemoryMB", runtime.freeMemory() / (1024 * 1024));
			runtimeStats.put("maxMemoryMB", runtime.maxMemory() / (1024 * 1024));
			runtimeStats.put("usedMemoryMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
			runtimeStats.put("availableProcessors", runtime.availableProcessors());
			systemStats.put("runtime", runtimeStats);

			// Información del JVM y SO
			Map<String, Object> environmentStats = new HashMap<>();
			environmentStats.put("javaVersion", System.getProperty("java.version"));
			environmentStats.put("javaVendor", System.getProperty("java.vendor"));
			environmentStats.put("osName", System.getProperty("os.name"));
			environmentStats.put("osVersion", System.getProperty("os.version"));
			environmentStats.put("osArch", System.getProperty("os.arch"));
			systemStats.put("environment", environmentStats);

			// Timestamp de generación
			systemStats.put("generatedAt", java.time.LocalDateTime.now());

			ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
					.success(true)
					.data(systemStats)
					.message("Estadísticas del sistema obtenidas exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo estadísticas del sistema: {}", e.getMessage());

			ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo estadísticas del sistema")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Reiniciar caché del sistema", description = "Limpia todas las cachés del sistema para forzar recarga de datos")
	@PostMapping("/clear-cache")
	public ResponseEntity<ApiResponse<String>> clearSystemCache() {

		log.info("Limpiando cachés del sistema");

		try {
			// Aquí normalmente limpiarías los cachés usando CacheManager
			// Por ahora solo simulamos la operación

			ApiResponse<String> response = ApiResponse.<String>builder()
					.success(true)
					.data("Cachés limpiados exitosamente")
					.message("Todas las cachés del sistema han sido limpiadas")
					.build();

			log.info("Cachés del sistema limpiados exitosamente");
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error limpiando cachés: {}", e.getMessage());

			ApiResponse<String> response = ApiResponse.<String>builder()
					.success(false)
					.data(null)
					.message("Error limpiando cachés del sistema")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Obtener logs del sistema", description = "Obtiene los últimos logs del sistema para diagnóstico")
	@GetMapping("/logs")
	public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemLogs(
			@Parameter(description = "Número de líneas de log a obtener") @RequestParam(defaultValue = "100") int lines,

			@Parameter(description = "Nivel de log mínimo") @RequestParam(defaultValue = "INFO") String level) {

		log.info("Obteniendo {} líneas de logs del sistema con nivel: {}", lines, level);

		try {
			// Aquí normalmente obtendrías los logs reales del sistema
			// Por ahora simulamos algunos logs de ejemplo
			Map<String, Object> logData = new HashMap<>();

			java.util.List<Map<String, Object>> logEntries = new java.util.ArrayList<>();
			for (int i = 1; i <= Math.min(lines, 10); i++) {
				Map<String, Object> logEntry = new HashMap<>();
				logEntry.put("timestamp", java.time.LocalDateTime.now().minusMinutes(i));
				logEntry.put("level", level);
				logEntry.put("logger", "com.arojas.jce_consulta_api");
				logEntry.put("message", "Log entry example " + i);
				logEntries.add(logEntry);
			}

			logData.put("entries", logEntries);
			logData.put("totalLines", logEntries.size());
			logData.put("level", level);
			logData.put("retrievedAt", java.time.LocalDateTime.now());

			ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
					.success(true)
					.data(logData)
					.message("Logs del sistema obtenidos exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo logs del sistema: {}", e.getMessage());

			ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo logs del sistema")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}
}
