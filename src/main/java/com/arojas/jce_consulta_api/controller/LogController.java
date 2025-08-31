/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arojas.jce_consulta_api.aspect.LogExecution;
import com.arojas.jce_consulta_api.entity.LogEntry;
import com.arojas.jce_consulta_api.entity.LogEntry.LogLevel;
import com.arojas.jce_consulta_api.repository.LogEntryRepository;
import com.arojas.jce_consulta_api.service.DbLoggerService;
import com.arojas.jce_consulta_api.service.LogAnalyticsService;
import com.arojas.jce_consulta_api.service.LogCleanupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author arojas
 *         * Controlador REST para gestión y consulta de logs
 *
 */

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
@Tag(name = "Log Management", description = "API para gestión y análisis de logs del sistema")
public class LogController {

	private final LogEntryRepository logEntryRepository;
	private final DbLoggerService dbLoggerService;
	private final LogAnalyticsService logAnalyticsService;
	private final LogCleanupService logCleanupService;

	/**
	 * Busca logs con filtros avanzados
	 */
	@GetMapping("/search")
	@Operation(summary = "Búsqueda avanzada de logs", description = "Permite buscar logs aplicando múltiples filtros")
	@LogExecution(operation = "SEARCH_LOGS")
	public ResponseEntity<Page<LogEntry>> searchLogs(
			@Parameter(description = "Nivel de log") @RequestParam(required = false) LogLevel level,
			@Parameter(description = "Email del usuario") @RequestParam(required = false) String userEmail,
			@Parameter(description = "Fuente del log") @RequestParam(required = false) String source,
			@Parameter(description = "Operación") @RequestParam(required = false) String operation,
			@Parameter(description = "Fecha de inicio") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@Parameter(description = "Fecha de fin") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
			@Parameter(description = "ID de correlación") @RequestParam(required = false) String correlationId,
			@Parameter(description = "ID de sesión") @RequestParam(required = false) String sessionId,
			@Parameter(description = "Incluir archivados") @RequestParam(required = false, defaultValue = "false") Boolean archived,
			@Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {

		Pageable pageable = PageRequest.of(page, size);
		Page<LogEntry> logs = logEntryRepository.searchLogs(
				level, userEmail, source, operation, start, end,
				correlationId, sessionId, archived, pageable);

		return ResponseEntity.ok(logs);
	}

	/**
	 * Búsqueda de texto completo
	 */
	@GetMapping("/full-text-search")
	@Operation(summary = "Búsqueda de texto completo")
	@LogExecution(operation = "FULL_TEXT_SEARCH")
	public ResponseEntity<Page<LogEntry>> fullTextSearch(
			@Parameter(description = "Texto a buscar") @RequestParam String searchText,
			@Parameter(description = "Buscar desde") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
			@Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {

		if (since == null) {
			since = LocalDateTime.now().minusDays(7); // Buscar en la última semana por defecto
		}

		Pageable pageable = PageRequest.of(page, size);
		Page<LogEntry> logs = logEntryRepository.fullTextSearch(searchText, since, pageable);

		return ResponseEntity.ok(logs);
	}

	/**
	 * Obtiene logs por correlation ID
	 */
	@GetMapping("/trace/{correlationId}")
	@Operation(summary = "Traza completa de un request")
	@LogExecution(operation = "GET_TRACE")
	public ResponseEntity<java.util.List<LogEntry>> getLogsByCorrelationId(
			@Parameter(description = "ID de correlación") @PathVariable String correlationId) {

		java.util.List<LogEntry> logs = logEntryRepository.findByCorrelationIdOrderByTimestampAsc(correlationId);
		return ResponseEntity.ok(logs);
	}

	/**
	 * Obtiene errores recientes
	 */
	@GetMapping("/errors")
	@Operation(summary = "Obtiene errores recientes")
	@LogExecution(operation = "GET_RECENT_ERRORS")
	public ResponseEntity<Page<LogEntry>> getRecentErrors(
			@Parameter(description = "Desde cuando buscar") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
			@Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {

		if (since == null) {
			since = LocalDateTime.now().minusHours(24); // Últimas 24 horas por defecto
		}

		Pageable pageable = PageRequest.of(page, size);
		Page<LogEntry> errors = logEntryRepository.findRecentErrors(since, pageable);

		return ResponseEntity.ok(errors);
	}

	/**
	 * Obtiene operaciones lentas
	 */
	@GetMapping("/slow-operations")
	@Operation(summary = "Obtiene operaciones lentas")
	@LogExecution(operation = "GET_SLOW_OPERATIONS")
	public ResponseEntity<Page<LogEntry>> getSlowOperations(
			@Parameter(description = "Umbral en ms") @RequestParam(defaultValue = "1000") Long thresholdMs,
			@Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {

		Pageable pageable = PageRequest.of(page, size);
		Page<LogEntry> slowOps = logEntryRepository.findSlowOperations(thresholdMs, pageable);

		return ResponseEntity.ok(slowOps);
	}

	/**
	 * Obtiene métricas del sistema
	 */
	@GetMapping("/metrics")
	@Operation(summary = "Métricas generales del sistema")
	@LogExecution(operation = "GET_SYSTEM_METRICS")
	public ResponseEntity<Map<String, Object>> getSystemMetrics(
			@Parameter(description = "Desde cuando calcular métricas") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {

		if (since == null) {
			since = LocalDateTime.now().minusHours(24); // Últimas 24 horas por defecto
		}

		Map<String, Object> metrics = logAnalyticsService.getSystemMetrics(since);
		return ResponseEntity.ok(metrics);
	}

	/**
	 * Obtiene reporte de salud del sistema
	 */
	@GetMapping("/health-report")
	@Operation(summary = "Reporte de salud del sistema")
	@LogExecution(operation = "GET_HEALTH_REPORT")
	public ResponseEntity<Map<String, Object>> getHealthReport() {
		Map<String, Object> report = logAnalyticsService.getSystemHealthReport();
		return ResponseEntity.ok(report);
	}

	/**
	 * Obtiene estadísticas de errores
	 */
	@GetMapping("/error-statistics")
	@Operation(summary = "Estadísticas detalladas de errores")
	@LogExecution(operation = "GET_ERROR_STATISTICS")
	public ResponseEntity<Map<String, Object>> getErrorStatistics(
			@Parameter(description = "Fecha de inicio") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@Parameter(description = "Fecha de fin") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

		if (start == null) {
			start = LocalDateTime.now().minusDays(7); // Última semana por defecto
		}
		if (end == null) {
			end = LocalDateTime.now();
		}

		Map<String, Object> statistics = logAnalyticsService.getErrorStatistics(start, end);
		return ResponseEntity.ok(statistics);
	}

	/**
	 * Obtiene actividad de usuarios
	 */
	@GetMapping("/user-activity")
	@Operation(summary = "Actividad de usuarios en el sistema")
	@LogExecution(operation = "GET_USER_ACTIVITY")
	public ResponseEntity<java.util.List<Object[]>> getUserActivity(
			@Parameter(description = "Desde cuando calcular actividad") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {

		if (since == null) {
			since = LocalDateTime.now().minusDays(1); // Último día por defecto
		}

		java.util.List<Object[]> activity = logAnalyticsService.getUserActivity(since);
		return ResponseEntity.ok(activity);
	}

	/**
	 * Obtiene tendencias de logs
	 */
	@GetMapping("/trends")
	@Operation(summary = "Tendencias de logs por nivel")
	@LogExecution(operation = "GET_LOG_TRENDS")
	public ResponseEntity<Map<String, Long>> getLogTrends(
			@Parameter(description = "Fecha de inicio") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@Parameter(description = "Fecha de fin") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

		if (start == null) {
			start = LocalDateTime.now().minusDays(7); // Última semana por defecto
		}
		if (end == null) {
			end = LocalDateTime.now();
		}

		Map<String, Long> trends = logAnalyticsService.getLogTrends(start, end);
		return ResponseEntity.ok(trends);
	}

	/**
	 * Crea un log manual (para testing)
	 */
	@PostMapping("/create")
	@Operation(summary = "Crea un log manual")
	@LogExecution(operation = "CREATE_MANUAL_LOG")
	public ResponseEntity<String> createLog(
			@Parameter(description = "Nivel del log") @RequestParam LogLevel level,
			@Parameter(description = "Fuente") @RequestParam String source,
			@Parameter(description = "Mensaje") @RequestParam String message,
			@Parameter(description = "Email del usuario") @RequestParam(required = false) String userEmail,
			@Parameter(description = "Operación") @RequestParam(required = false) String operation) {

		dbLoggerService.log()
				.level(level)
				.source(source)
				.message(message)
				.user(userEmail)
				.operation(operation)
				.context("manuallyCreated", true)
				.context("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
				.save();

		return ResponseEntity.ok("Log creado exitosamente");
	}

	/**
	 * Ejecuta limpieza manual de logs
	 */
	@PostMapping("/cleanup/archive")
	@Operation(summary = "Ejecuta archivado manual de logs")
	@LogExecution(operation = "MANUAL_ARCHIVE")
	public ResponseEntity<Map<String, Object>> manualArchive() {
		int archivedCount = logCleanupService.archiveOldLogs();

		Map<String, Object> result = Map.of(
				"archivedCount", archivedCount,
				"executedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

		return ResponseEntity.ok(result);
	}

	/**
	 * Ejecuta eliminación manual de logs archivados
	 */
	@PostMapping("/cleanup/delete")
	@Operation(summary = "Ejecuta eliminación manual de logs archivados")
	@LogExecution(operation = "MANUAL_DELETE")
	public ResponseEntity<Map<String, Object>> manualDelete() {
		int deletedCount = logCleanupService.deleteOldArchivedLogs();

		Map<String, Object> result = Map.of(
				"deletedCount", deletedCount,
				"executedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

		return ResponseEntity.ok(result);
	}

	/**
	 * Obtiene estadísticas de limpieza
	 */
	@GetMapping("/cleanup/statistics")
	@Operation(summary = "Estadísticas de limpieza de logs")
	@LogExecution(operation = "GET_CLEANUP_STATS")
	public ResponseEntity<Map<String, Object>> getCleanupStatistics() {
		Map<String, Object> stats = logCleanupService.getCleanupStatistics();
		return ResponseEntity.ok(stats);
	}

	/**
	 * Obtiene un log específico por ID
	 */
	@GetMapping("/{id}")
	@Operation(summary = "Obtiene un log específico")
	@LogExecution(operation = "GET_LOG_BY_ID")
	public ResponseEntity<LogEntry> getLogById(
			@Parameter(description = "ID del log") @PathVariable Long id) {

		return logEntryRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Exporta logs como CSV
	 */
	@GetMapping("/export/csv")
	@Operation(summary = "Exporta logs como CSV")
	@LogExecution(operation = "EXPORT_LOGS_CSV")
	public ResponseEntity<String> exportLogsAsCsv(
			@Parameter(description = "Fecha de inicio") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
			@Parameter(description = "Fecha de fin") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
			@Parameter(description = "Nivel de log") @RequestParam(required = false) LogLevel level) {

		if (start == null) {
			start = LocalDateTime.now().minusDays(1);
		}
		if (end == null) {
			end = LocalDateTime.now();
		}

		Pageable pageable = PageRequest.of(0, 10000); // Máximo 10k registros
		Page<LogEntry> logs = logEntryRepository.searchLogs(
				level, null, null, null, start, end,
				null, null, false, pageable);

		StringBuilder csv = new StringBuilder();
		csv.append("ID,Timestamp,Level,Source,Message,UserEmail,Operation,ExecutionTimeMs\n");

		for (LogEntry log : logs.getContent()) {
			csv.append(String.format("%d,%s,%s,%s,\"%s\",%s,%s,%s\n",
					log.getId(),
					log.getTimestamp(),
					log.getLevel(),
					log.getSource(),
					log.getMessage().replace("\"", "\"\""), // Escapar comillas
					log.getUserEmail() != null ? log.getUserEmail() : "",
					log.getOperation() != null ? log.getOperation() : "",
					log.getExecutionTimeMs() != null ? log.getExecutionTimeMs() : ""));
		}

		return ResponseEntity.ok()
				.header("Content-Type", "text/csv")
				.header("Content-Disposition", "attachment; filename=\"logs_export.csv\"")
				.body(csv.toString());
	}
}
