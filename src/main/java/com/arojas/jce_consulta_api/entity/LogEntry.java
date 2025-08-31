package com.arojas.jce_consulta_api.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad para el registro de logs de la aplicación
 * Proporciona trazabilidad completa y contexto para debugging
 * 
 * @author arojas
 */
@Entity
@Table(name = "application_logs", indexes = {
		@Index(name = "idx_log_level_timestamp", columnList = "level, timestamp"),
		@Index(name = "idx_log_user_timestamp", columnList = "user_email, timestamp"),
		@Index(name = "idx_log_source_timestamp", columnList = "source, timestamp"),
		@Index(name = "idx_log_correlation_id", columnList = "correlation_id"),
		@Index(name = "idx_log_session_id", columnList = "session_id")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(nullable = false, updatable = false)
	@Builder.Default
	private LocalDateTime timestamp = LocalDateTime.now();

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private LogLevel level;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String source; // Clase o módulo que generó el log

	@NotBlank
	@Size(max = 1000)
	@Column(nullable = false, length = 1000)
	private String message;

	@Size(max = 255)
	@Column(name = "user_email", length = 255)
	private String userEmail;

	@Size(max = 50)
	@Column(name = "session_id", length = 50)
	private String sessionId;

	@Size(max = 50)
	@Column(name = "correlation_id", length = 50)
	private String correlationId; // Para trazar requests completos

	@Size(max = 50)
	@Column(name = "request_id", length = 50)
	private String requestId;

	@Size(max = 100)
	@Column(name = "operation", length = 100)
	private String operation; // Operación específica (login, createUser, etc.)

	@Size(max = 15)
	@Column(name = "client_ip", length = 15)
	private String clientIp;

	@Size(max = 500)
	@Column(name = "user_agent", length = 500)
	private String userAgent;

	@Column(name = "execution_time_ms")
	private Long executionTimeMs; // Tiempo de ejecución en ms

	@Lob
	@Column(name = "stack_trace", columnDefinition = "TEXT")
	private String stackTrace;

	@Lob
	@Column(name = "request_payload", columnDefinition = "TEXT")
	private String requestPayload; // Payload del request (sanitizado)

	@Lob
	@Column(name = "response_payload", columnDefinition = "TEXT")
	private String responsePayload; // Response (sanitizado)

	// Contexto adicional como JSON
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "context", columnDefinition = "JSON")
	private Map<String, Object> context;

	@Size(max = 50)
	@Column(name = "environment", length = 50)
	@Builder.Default
	private String environment = "PRODUCTION"; // DEV, TEST, STAGING, PRODUCTION

	@Size(max = 20)
	@Column(name = "application_version", length = 20)
	private String applicationVersion;

	@Builder.Default
	@Column(name = "archived")
	private Boolean archived = false;

	/**
	 * Enum para los niveles de log
	 */
	public enum LogLevel {
		TRACE("TRACE", 0),
		DEBUG("DEBUG", 1),
		INFO("INFO", 2),
		WARN("WARN", 3),
		ERROR("ERROR", 4),
		FATAL("FATAL", 5);

		private final String name;
		private final int priority;

		LogLevel(String name, int priority) {
			this.name = name;
			this.priority = priority;
		}

		public String getName() {
			return name;
		}

		public int getPriority() {
			return priority;
		}

		public boolean isEqualOrHigherThan(LogLevel other) {
			return this.priority >= other.priority;
		}
	}

	/**
	 * Métodos de conveniencia
	 */
	public boolean isErrorLevel() {
		return level == LogLevel.ERROR || level == LogLevel.FATAL;
	}

	public boolean isDebugLevel() {
		return level == LogLevel.DEBUG || level == LogLevel.TRACE;
	}

	@PrePersist
	public void prePersist() {
		if (timestamp == null) {
			timestamp = LocalDateTime.now();
		}
		if (archived == null) {
			archived = false;
		}
	}
}