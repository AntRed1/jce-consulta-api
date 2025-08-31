package com.arojas.jce_consulta_api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arojas.jce_consulta_api.entity.LogEntry;
import com.arojas.jce_consulta_api.entity.LogEntry.LogLevel;

/**
 * Repositorio avanzado para manejo de logs con capacidades de búsqueda,
 * filtrado, paginación y consultas optimizadas.
 * 
 * @author arojas
 */
@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, Long>,
		JpaSpecificationExecutor<LogEntry> {

	// ============= CONSULTAS BÁSICAS =============

	/**
	 * Buscar logs por nivel ordenados por timestamp descendente
	 */
	Page<LogEntry> findByLevelOrderByTimestampDesc(LogLevel level, Pageable pageable);

	List<LogEntry> findByLevelOrderByTimestampDesc(LogLevel level);

	/**
	 * Buscar logs por usuario
	 */
	Page<LogEntry> findByUserEmailOrderByTimestampDesc(String userEmail, Pageable pageable);

	/**
	 * Buscar logs por módulo/fuente
	 */
	Page<LogEntry> findBySourceContainingIgnoreCaseOrderByTimestampDesc(String source, Pageable pageable);

	/**
	 * Buscar logs por rango de fechas
	 */
	Page<LogEntry> findByTimestampBetweenOrderByTimestampDesc(
			LocalDateTime start, LocalDateTime end, Pageable pageable);

	/**
	 * Buscar logs por correlation ID para trazar requests completos
	 */
	List<LogEntry> findByCorrelationIdOrderByTimestampAsc(String correlationId);

	/**
	 * Buscar logs por session ID
	 */
	List<LogEntry> findBySessionIdOrderByTimestampDesc(String sessionId);

	/**
	 * Buscar logs por operación
	 */
	Page<LogEntry> findByOperationContainingIgnoreCaseOrderByTimestampDesc(
			String operation, Pageable pageable);

	// ============= CONSULTAS DE RENDIMIENTO =============

	/**
	 * Buscar operaciones lentas (tiempo de ejecución mayor al especificado)
	 */
	@Query("""
			SELECT l FROM LogEntry l
			WHERE l.executionTimeMs > :thresholdMs
			AND l.executionTimeMs IS NOT NULL
			ORDER BY l.executionTimeMs DESC
			""")
	Page<LogEntry> findSlowOperations(@Param("thresholdMs") Long thresholdMs, Pageable pageable);

	/**
	 * Obtener estadísticas de rendimiento por operación
	 */
	@Query("""
			SELECT l.operation,
			       AVG(l.executionTimeMs) as avgTime,
			       MIN(l.executionTimeMs) as minTime,
			       MAX(l.executionTimeMs) as maxTime,
			       COUNT(l) as totalOps
			FROM LogEntry l
			WHERE l.executionTimeMs IS NOT NULL
			AND l.operation IS NOT NULL
			AND l.timestamp >= :since
			GROUP BY l.operation
			ORDER BY avgTime DESC
			""")
	List<Object[]> getPerformanceStatsByOperation(@Param("since") LocalDateTime since);

	// ============= CONSULTAS DE ERRORES =============

	/**
	 * Buscar todos los errores y fatales
	 */
	@Query("""
			SELECT l FROM LogEntry l
			WHERE l.level IN ('ERROR', 'FATAL')
			AND l.timestamp >= :since
			ORDER BY l.timestamp DESC
			""")
	Page<LogEntry> findRecentErrors(@Param("since") LocalDateTime since, Pageable pageable);

	/**
	 * Contar errores por fuente en un período
	 */
	@Query("""
			SELECT l.source, COUNT(l) as errorCount
			FROM LogEntry l
			WHERE l.level IN ('ERROR', 'FATAL')
			AND l.timestamp BETWEEN :start AND :end
			GROUP BY l.source
			ORDER BY errorCount DESC
			""")
	List<Object[]> countErrorsBySource(
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);

	/**
	 * Buscar errores recurrentes (mismo mensaje de error)
	 */
	@Query("""
			SELECT l.message, l.source, COUNT(l) as occurrences,
			       MIN(l.timestamp) as firstOccurrence,
			       MAX(l.timestamp) as lastOccurrence
			FROM LogEntry l
			WHERE l.level IN ('ERROR', 'FATAL')
			AND l.timestamp >= :since
			GROUP BY l.message, l.source
			HAVING COUNT(l) > :minOccurrences
			ORDER BY occurrences DESC
			""")
	List<Object[]> findRecurringErrors(
			@Param("since") LocalDateTime since,
			@Param("minOccurrences") Long minOccurrences);

	// ============= CONSULTAS AVANZADAS =============

	/**
	 * Búsqueda completa y flexible
	 */
	@Query("""
			SELECT l FROM LogEntry l
			WHERE (:level IS NULL OR l.level = :level)
			  AND (:userEmail IS NULL OR l.userEmail = :userEmail)
			  AND (:source IS NULL OR LOWER(l.source) LIKE LOWER(CONCAT('%', :source, '%')))
			  AND (:operation IS NULL OR LOWER(l.operation) LIKE LOWER(CONCAT('%', :operation, '%')))
			  AND (:start IS NULL OR l.timestamp >= :start)
			  AND (:end IS NULL OR l.timestamp <= :end)
			  AND (:correlationId IS NULL OR l.correlationId = :correlationId)
			  AND (:sessionId IS NULL OR l.sessionId = :sessionId)
			  AND (:archived IS NULL OR l.archived = :archived)
			ORDER BY l.timestamp DESC
			""")
	Page<LogEntry> searchLogs(
			@Param("level") LogLevel level,
			@Param("userEmail") String userEmail,
			@Param("source") String source,
			@Param("operation") String operation,
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end,
			@Param("correlationId") String correlationId,
			@Param("sessionId") String sessionId,
			@Param("archived") Boolean archived,
			Pageable pageable);

	/**
	 * Búsqueda de texto completo en mensaje y stack trace (case-insensitive con
	 * consulta nativa)
	 */
	@Query(value = """
			SELECT * FROM application_logs l
			WHERE (UPPER(CAST(l.message AS VARCHAR(4000))) LIKE UPPER(CONCAT('%', :searchText, '%'))
			   OR UPPER(CAST(l.stack_trace AS VARCHAR(4000))) LIKE UPPER(CONCAT('%', :searchText, '%')))
			AND l.timestamp >= :since
			ORDER BY l.timestamp DESC
			""", nativeQuery = true, countQuery = """
				SELECT COUNT(*) FROM application_logs l
				WHERE (UPPER(CAST(l.message AS VARCHAR(4000))) LIKE UPPER(CONCAT('%', :searchText, '%'))
				   OR UPPER(CAST(l.stack_trace AS VARCHAR(4000))) LIKE UPPER(CONCAT('%', :searchText, '%')))
				AND l.timestamp >= :since
			""")
	Page<LogEntry> fullTextSearch(
			@Param("searchText") String searchText,
			@Param("since") LocalDateTime since,
			Pageable pageable);

	// ============= CONSULTAS DE ESTADÍSTICAS =============

	/**
	 * Contar logs por nivel en un período
	 */
	@Query("""
			SELECT l.level, COUNT(l) as count
			FROM LogEntry l
			WHERE l.timestamp BETWEEN :start AND :end
			GROUP BY l.level
			ORDER BY count DESC
			""")
	List<Object[]> getLogCountByLevel(
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);

	/**
	 * Obtener actividad por usuario en un período
	 */
	@Query("""
			SELECT l.userEmail, COUNT(l) as activityCount,
			       COUNT(CASE WHEN l.level IN ('ERROR', 'FATAL') THEN 1 END) as errorCount
			FROM LogEntry l
			WHERE l.userEmail IS NOT NULL
			AND l.timestamp >= :since
			GROUP BY l.userEmail
			ORDER BY activityCount DESC
			""")
	List<Object[]> getUserActivity(@Param("since") LocalDateTime since);

	/**
	 * Obtener logs recientes por nivel
	 */
	@Query("""
			SELECT l FROM LogEntry l
			WHERE l.level = :level
			AND l.timestamp >= :since
			ORDER BY l.timestamp DESC
			LIMIT :limit
			""")
	List<LogEntry> getRecentLogsByLevel(
			@Param("level") LogLevel level,
			@Param("since") LocalDateTime since,
			@Param("limit") int limit);

	// ============= CONSULTAS DE MANTENIMIENTO =============

	/**
	 * Archivar logs antiguos
	 */
	@Modifying
	@Query("""
			UPDATE LogEntry l
			SET l.archived = true
			WHERE l.timestamp < :archiveDate
			AND l.archived = false
			""")
	int archiveOldLogs(@Param("archiveDate") LocalDateTime archiveDate);

	/**
	 * Eliminar logs archivados antiguos
	 */
	@Modifying
	@Query("""
			DELETE FROM LogEntry l
			WHERE l.archived = true
			AND l.timestamp < :deleteDate
			""")
	int deleteArchivedLogs(@Param("deleteDate") LocalDateTime deleteDate);

	/**
	 * Contar logs por estado de archivo
	 */
	@Query("SELECT l.archived, COUNT(l) FROM LogEntry l GROUP BY l.archived")
	List<Object[]> getArchiveStatistics();

	/**
	 * Obtener el log más reciente por fuente
	 */
	Optional<LogEntry> findFirstBySourceOrderByTimestampDesc(String source);

	/**
	 * Verificar si existe un correlation ID
	 */
	boolean existsByCorrelationId(String correlationId);

	// ============= CONSULTAS PARA DASHBOARD =============

	/**
	 * Obtener resumen de logs para dashboard
	 */
	@Query("""
			SELECT
			    COUNT(l) as total,
			    COUNT(CASE WHEN l.level = 'ERROR' THEN 1 END) as errors,
			    COUNT(CASE WHEN l.level = 'FATAL' THEN 1 END) as fatals,
			    COUNT(CASE WHEN l.level = 'WARN' THEN 1 END) as warnings,
			    COUNT(CASE WHEN l.level = 'INFO' THEN 1 END) as infos,
			    COUNT(DISTINCT l.userEmail) as activeUsers,
			    COUNT(DISTINCT l.source) as activeSources
			FROM LogEntry l
			WHERE l.timestamp >= :since
			""")
	List<Object[]> getDashboardSummary(@Param("since") LocalDateTime since);

	/**
	 * Obtener tendencia de logs por hora
	 */
	@Query(value = """
			SELECT
			    DATE_TRUNC('hour', timestamp) as hour,
			    level,
			    COUNT(*) as count
			FROM application_logs
			WHERE timestamp >= :since
			GROUP BY DATE_TRUNC('hour', timestamp), level
			ORDER BY hour DESC
			""", nativeQuery = true)
	List<Object[]> getLogTrendByHour(@Param("since") LocalDateTime since);
}