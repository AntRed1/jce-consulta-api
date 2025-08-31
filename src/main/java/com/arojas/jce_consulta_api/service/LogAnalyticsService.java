/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.arojas.jce_consulta_api.entity.LogEntry;
import com.arojas.jce_consulta_api.entity.LogEntry.LogLevel;
import com.arojas.jce_consulta_api.repository.LogEntryRepository;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author arojas
 *         * Servicio para análisis y métricas de logs
 *
 */

@Service
@RequiredArgsConstructor
public class LogAnalyticsService {

	private final LogEntryRepository logEntryRepository;

	/**
	 * Obtiene métricas generales del sistema
	 */
	public Map<String, Object> getSystemMetrics(LocalDateTime since) {
		Map<String, Object> metrics = new HashMap<>();

		// Resumen general
		List<Object[]> summary = logEntryRepository.getDashboardSummary(since);
		if (!summary.isEmpty()) {
			Object[] data = summary.get(0);
			metrics.put("totalLogs", data[0]);
			metrics.put("errors", data[1]);
			metrics.put("fatals", data[2]);
			metrics.put("warnings", data[3]);
			metrics.put("infos", data[4]);
			metrics.put("activeUsers", data[5]);
			metrics.put("activeSources", data[6]);
		}

		// Errores recurrentes
		List<Object[]> recurringErrors = logEntryRepository.findRecurringErrors(since, 3L);
		metrics.put("recurringErrors", recurringErrors);

		// Operaciones lentas
		Pageable topSlow = PageRequest.of(0, 10);
		Page<LogEntry> slowOps = logEntryRepository.findSlowOperations(1000L, topSlow);
		metrics.put("slowOperations", slowOps.getContent());

		// Estadísticas de rendimiento
		List<Object[]> perfStats = logEntryRepository.getPerformanceStatsByOperation(since);
		metrics.put("performanceStats", perfStats);

		return metrics;
	}

	/**
	 * Obtiene estadísticas de errores
	 */
	public Map<String, Object> getErrorStatistics(LocalDateTime start, LocalDateTime end) {
		Map<String, Object> stats = new HashMap<>();

		List<Object[]> errorsBySource = logEntryRepository.countErrorsBySource(start, end);
		stats.put("errorsBySource", errorsBySource);

		List<Object[]> recurringErrors = logEntryRepository.findRecurringErrors(start, 2L);
		stats.put("recurringErrors", recurringErrors);

		return stats;
	}

	/**
	 * Obtiene actividad de usuarios
	 */
	public List<Object[]> getUserActivity(LocalDateTime since) {
		return logEntryRepository.getUserActivity(since);
	}

	/**
	 * Obtiene tendencias por nivel de log
	 */
	public Map<String, Long> getLogTrends(LocalDateTime start, LocalDateTime end) {
		List<Object[]> counts = logEntryRepository.getLogCountByLevel(start, end);
		Map<String, Long> trends = new HashMap<>();

		for (Object[] count : counts) {
			LogLevel level = (LogLevel) count[0];
			Long total = (Long) count[1];
			trends.put(level.name(), total);
		}

		return trends;
	}

	/**
	 * Genera reporte de salud del sistema
	 */
	public Map<String, Object> getSystemHealthReport() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime last24Hours = now.minusHours(24);
		LocalDateTime lastHour = now.minusHours(1);

		Map<String, Object> report = new HashMap<>();

		// Métricas de la última hora
		Map<String, Object> lastHourMetrics = getSystemMetrics(lastHour);
		report.put("lastHour", lastHourMetrics);

		// Métricas de las últimas 24 horas
		Map<String, Object> last24HoursMetrics = getSystemMetrics(last24Hours);
		report.put("last24Hours", last24HoursMetrics);

		// Calcular tasas de error
		Long totalLogs24h = (Long) last24HoursMetrics.get("totalLogs");
		Long errors24h = (Long) last24HoursMetrics.get("errors");
		Long fatals24h = (Long) last24HoursMetrics.get("fatals");

		if (totalLogs24h > 0) {
			double errorRate = ((double) (errors24h + fatals24h) / totalLogs24h) * 100;
			report.put("errorRate24h", Math.round(errorRate * 100.0) / 100.0);
		}

		// Estado general
		String healthStatus = determineHealthStatus(errors24h, fatals24h, totalLogs24h);
		report.put("healthStatus", healthStatus);
		report.put("timestamp", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

		return report;
	}

	private String determineHealthStatus(Long errors, Long fatals, Long totalLogs) {
		if (fatals > 0)
			return "CRITICAL";
		if (totalLogs > 0) {
			double errorRate = ((double) errors / totalLogs) * 100;
			if (errorRate > 10)
				return "UNHEALTHY";
			if (errorRate > 5)
				return "WARNING";
		}
		return "HEALTHY";
	}
}
