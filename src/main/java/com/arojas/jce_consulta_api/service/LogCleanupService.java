/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arojas.jce_consulta_api.entity.LogEntry.LogLevel;
import com.arojas.jce_consulta_api.repository.LogEntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author arojas
 *         * Servicio para mantenimiento y limpieza automática de logs
 *
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class LogCleanupService {

	private final LogEntryRepository logEntryRepository;
	private final DbLoggerService dbLoggerService;

	@Value("${app.logging.cleanup.archive-after-days:30}")
	private int archiveAfterDays;

	@Value("${app.logging.cleanup.delete-after-days:90}")
	private int deleteAfterDays;

	@Value("${app.logging.cleanup.enabled:true}")
	private boolean cleanupEnabled;

	/**
	 * Ejecuta limpieza automática cada día a las 2:00 AM
	 */
	@Scheduled(cron = "0 0 2 * * ?")
	@Transactional
	public void performScheduledCleanup() {
		if (!cleanupEnabled) {
			return;
		}

		log.info("Iniciando limpieza automática de logs");

		try {
			// Archivar logs antiguos
			int archivedCount = archiveOldLogs();

			// Eliminar logs archivados muy antiguos
			int deletedCount = deleteOldArchivedLogs();

			dbLoggerService.info("LogCleanupService",
					String.format("Limpieza completada - Archivados: %d, Eliminados: %d",
							archivedCount, deletedCount));

		} catch (Exception e) {
			dbLoggerService.error("LogCleanupService",
					"Error durante la limpieza automática", e);
		}
	}

	/**
	 * Archiva logs antiguos
	 */
	public int archiveOldLogs() {
		LocalDateTime archiveDate = LocalDateTime.now().minusDays(archiveAfterDays);
		int count = logEntryRepository.archiveOldLogs(archiveDate);

		if (count > 0) {
			dbLoggerService.log()
					.level(LogLevel.INFO)
					.source("LogCleanupService")
					.operation("ARCHIVE_LOGS")
					.message(String.format("Archivados %d logs anteriores a %s", count, archiveDate))
					.context("archivedCount", count)
					.context("archiveDate", archiveDate.toString())
					.save();
		}

		return count;
	}

	/**
	 * Elimina logs archivados muy antiguos
	 */
	public int deleteOldArchivedLogs() {
		LocalDateTime deleteDate = LocalDateTime.now().minusDays(deleteAfterDays);
		int count = logEntryRepository.deleteArchivedLogs(deleteDate);

		if (count > 0) {
			dbLoggerService.log()
					.level(LogLevel.INFO)
					.source("LogCleanupService")
					.operation("DELETE_LOGS")
					.message(String.format("Eliminados %d logs archivados anteriores a %s", count, deleteDate))
					.context("deletedCount", count)
					.context("deleteDate", deleteDate.toString())
					.save();
		}

		return count;
	}

	/**
	 * Obtiene estadísticas de archivado
	 */
	public Map<String, Object> getCleanupStatistics() {
		List<Object[]> stats = logEntryRepository.getArchiveStatistics();
		Map<String, Object> result = new HashMap<>();

		for (Object[] stat : stats) {
			Boolean archived = (Boolean) stat[0];
			Long count = (Long) stat[1];
			result.put(archived ? "archived" : "active", count);
		}

		return result;
	}
}
