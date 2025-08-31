package com.arojas.jce_consulta_api.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.arojas.jce_consulta_api.entity.LogEntry;
import com.arojas.jce_consulta_api.entity.LogEntry.LogLevel;
import com.arojas.jce_consulta_api.repository.LogEntryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de logging profesional y escalable para registrar eventos en la base
 * de datos.
 * Proporciona funcionalidades avanzadas como contexto estructurado,
 * trazabilidad de requests,
 * logging asíncrono y sanitización automática de datos sensibles.
 * 
 * @author arojas
 */
@Service
@RequiredArgsConstructor
public class DbLoggerService {

	private static final Logger logger = LoggerFactory.getLogger(DbLoggerService.class);

	private final LogEntryRepository logEntryRepository;
	private final ObjectMapper objectMapper;

	@Value("${app.logging.enabled:true}")
	private boolean loggingEnabled;

	@Value("${app.logging.level:INFO}")
	private String minimumLogLevel;

	@Value("${app.logging.async:true}")
	private boolean asyncLogging;

	@Value("${app.version:1.0.0}")
	private String applicationVersion;

	@Value("${spring.profiles.active:production}")
	private String environment;

	// Campos sensibles que deben ser enmascarados
	private static final String[] SENSITIVE_FIELDS = {
			"password", "token", "authorization", "secret", "key",
			"creditCard", "ssn", "email", "phone"
	};

	/**
	 * Builder para construcción fluida de logs
	 */
	public LogBuilder log() {
		return new LogBuilder(this);
	}

	/**
	 * Métodos de conveniencia para logging rápido
	 */
	public void trace(String source, String message) {
		log().level(LogLevel.TRACE).source(source).message(message).save();
	}

	public void debug(String source, String message) {
		log().level(LogLevel.DEBUG).source(source).message(message).save();
	}

	public void info(String source, String message) {
		log().level(LogLevel.INFO).source(source).message(message).save();
	}

	public void warn(String source, String message) {
		log().level(LogLevel.WARN).source(source).message(message).save();
	}

	public void error(String source, String message, Throwable throwable) {
		log().level(LogLevel.ERROR).source(source).message(message).exception(throwable).save();
	}

	public void fatal(String source, String message, Throwable throwable) {
		log().level(LogLevel.FATAL).source(source).message(message).exception(throwable).save();
	}

	/**
	 * Logging con contexto de request HTTP
	 */
	public void logWithRequest(LogLevel level, String source, String message,
			HttpServletRequest request) {
		log().level(level)
				.source(source)
				.message(message)
				.withRequest(request)
				.save();
	}

	/**
	 * Logging de operaciones con tiempo de ejecución
	 */
	public void logOperation(String source, String operation, long executionTimeMs,
			boolean success, String details) {
		LogLevel level = success ? LogLevel.INFO : LogLevel.WARN;
		log().level(level)
				.source(source)
				.operation(operation)
				.executionTime(executionTimeMs)
				.message(success ? "Operation completed successfully" : "Operation completed with warnings")
				.context("success", success)
				.context("details", details)
				.save();
	}

	/**
	 * Método interno para persistir el log
	 */
	@Async("logTaskExecutor")
	@Transactional
	public CompletableFuture<Void> saveLogAsync(LogEntry logEntry) {
		try {
			if (!shouldLog(logEntry.getLevel())) {
				return CompletableFuture.completedFuture(null);
			}

			// Sanitizar datos sensibles
			sanitizeLogEntry(logEntry);

			// Validar campos requeridos
			if (logEntry.getSource() == null || logEntry.getSource().trim().isEmpty()) {
				logEntry.setSource("UNKNOWN");
			}
			if (logEntry.getMessage() == null || logEntry.getMessage().trim().isEmpty()) {
				logEntry.setMessage("No message provided");
			}

			logEntryRepository.save(logEntry);

		} catch (Exception e) {
			// Fallback logging - nunca debe fallar la aplicación por el logging
			logger.error("Error guardando log en BD - Source: {}, Message: {}, Error: {}",
					logEntry.getSource(), logEntry.getMessage(), e.getMessage());
		}
		return CompletableFuture.completedFuture(null);
	}

	@Transactional
	public void saveLogSync(LogEntry logEntry) {
		try {
			if (!shouldLog(logEntry.getLevel())) {
				return;
			}

			sanitizeLogEntry(logEntry);

			// Validar campos requeridos
			if (logEntry.getSource() == null || logEntry.getSource().trim().isEmpty()) {
				logEntry.setSource("UNKNOWN");
			}
			if (logEntry.getMessage() == null || logEntry.getMessage().trim().isEmpty()) {
				logEntry.setMessage("No message provided");
			}

			logEntryRepository.save(logEntry);

		} catch (Exception e) {
			logger.error("Error guardando log en BD - Source: {}, Message: {}, Error: {}",
					logEntry.getSource(), logEntry.getMessage(), e.getMessage());
		}
	}

	/**
	 * Determina si el log debe ser procesado basado en el nivel configurado
	 */
	private boolean shouldLog(LogLevel level) {
		if (!loggingEnabled)
			return false;

		try {
			LogLevel minimumLevel = LogLevel.valueOf(minimumLogLevel.toUpperCase());
			return level.isEqualOrHigherThan(minimumLevel);
		} catch (IllegalArgumentException e) {
			return true; // Default: log everything
		}
	}

	/**
	 * Sanitiza datos sensibles del log
	 */
	private void sanitizeLogEntry(LogEntry logEntry) {
		// Sanitizar mensaje
		if (logEntry.getMessage() != null) {
			logEntry.setMessage(sanitizeString(logEntry.getMessage()));
		}

		// Sanitizar payloads
		if (logEntry.getRequestPayload() != null) {
			logEntry.setRequestPayload(sanitizeString(logEntry.getRequestPayload()));
		}

		if (logEntry.getResponsePayload() != null) {
			logEntry.setResponsePayload(sanitizeString(logEntry.getResponsePayload()));
		}

		// Sanitizar contexto
		if (logEntry.getContext() != null) {
			logEntry.setContext(sanitizeContext(logEntry.getContext()));
		}
	}

	/**
	 * Sanitiza strings removiendo información sensible
	 */
	private String sanitizeString(String input) {
		if (!StringUtils.hasText(input))
			return input;

		String sanitized = input;
		for (String field : SENSITIVE_FIELDS) {
			sanitized = sanitized.replaceAll("(?i)" + field + "\"?\\s*[:=]\\s*\"?[^\\s,}]+",
					field + ":***MASKED***");
		}
		return sanitized;
	}

	/**
	 * Sanitiza el contexto JSON
	 */
	private Map<String, Object> sanitizeContext(Map<String, Object> context) {
		Map<String, Object> sanitized = new HashMap<>();

		context.forEach((key, value) -> {
			boolean isSensitive = false;
			for (String sensitiveField : SENSITIVE_FIELDS) {
				if (key.toLowerCase().contains(sensitiveField.toLowerCase())) {
					isSensitive = true;
					break;
				}
			}

			if (isSensitive) {
				sanitized.put(key, "***MASKED***");
			} else {
				sanitized.put(key, value);
			}
		});

		return sanitized;
	}

	/**
	 * Extrae información del stack trace
	 */
	private String extractStackTrace(Throwable throwable) {
		if (throwable == null)
			return null;

		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		throwable.printStackTrace(printWriter);
		return stringWriter.toString();
	}

	/**
	 * Convierte objeto a JSON de forma segura
	 */
	private String toJsonString(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			return "Error serializing object: " + e.getMessage();
		}
	}

	/**
	 * Builder pattern para construcción fluida de logs
	 */
	public static class LogBuilder {
		private final DbLoggerService loggerService;
		private final LogEntry logEntry;

		public LogBuilder(DbLoggerService loggerService) {
			this.loggerService = loggerService;
			this.logEntry = LogEntry.builder()
					.timestamp(LocalDateTime.now())
					.environment(loggerService.environment)
					.applicationVersion(loggerService.applicationVersion)
					.correlationId(MDC.get("correlationId"))
					.requestId(MDC.get("requestId"))
					.sessionId(MDC.get("sessionId"))
					.userEmail(MDC.get("userEmail"))
					.build();
		}

		public LogBuilder level(LogLevel level) {
			logEntry.setLevel(level);
			return this;
		}

		public LogBuilder source(String source) {
			logEntry.setSource(source);
			return this;
		}

		public LogBuilder message(String message) {
			logEntry.setMessage(message);
			return this;
		}

		public LogBuilder operation(String operation) {
			logEntry.setOperation(operation);
			return this;
		}

		public LogBuilder user(String userEmail) {
			logEntry.setUserEmail(userEmail);
			return this;
		}

		public LogBuilder correlationId(String correlationId) {
			logEntry.setCorrelationId(correlationId);
			return this;
		}

		public LogBuilder sessionId(String sessionId) {
			logEntry.setSessionId(sessionId);
			return this;
		}

		public LogBuilder executionTime(long executionTimeMs) {
			logEntry.setExecutionTimeMs(executionTimeMs);
			return this;
		}

		public LogBuilder exception(Throwable throwable) {
			if (throwable != null) {
				logEntry.setStackTrace(loggerService.extractStackTrace(throwable));
				context("exceptionType", throwable.getClass().getSimpleName());
				context("exceptionMessage", throwable.getMessage());
			}
			return this;
		}

		public LogBuilder requestPayload(Object payload) {
			logEntry.setRequestPayload(loggerService.toJsonString(payload));
			return this;
		}

		public LogBuilder responsePayload(Object payload) {
			logEntry.setResponsePayload(loggerService.toJsonString(payload));
			return this;
		}

		public LogBuilder withRequest(HttpServletRequest request) {
			if (request != null) {
				logEntry.setClientIp(getClientIpAddress(request));
				logEntry.setUserAgent(request.getHeader("User-Agent"));
				logEntry.setRequestId(request.getHeader("X-Request-ID"));

				// Generar correlation ID si no existe
				if (logEntry.getCorrelationId() == null) {
					logEntry.setCorrelationId(UUID.randomUUID().toString().substring(0, 8));
				}
			}
			return this;
		}

		public LogBuilder context(String key, Object value) {
			if (logEntry.getContext() == null) {
				logEntry.setContext(new HashMap<>());
			}
			logEntry.getContext().put(key, value);
			return this;
		}

		public LogBuilder context(Map<String, Object> contextMap) {
			if (contextMap != null) {
				if (logEntry.getContext() == null) {
					logEntry.setContext(new HashMap<>());
				}
				logEntry.getContext().putAll(contextMap);
			}
			return this;
		}

		public void save() {
			if (loggerService.asyncLogging) {
				loggerService.saveLogAsync(logEntry);
			} else {
				loggerService.saveLogSync(logEntry);
			}
		}

		private String getClientIpAddress(HttpServletRequest request) {
			String xForwardedFor = request.getHeader("X-Forwarded-For");
			if (StringUtils.hasText(xForwardedFor)) {
				return xForwardedFor.split(",")[0].trim();
			}

			String xRealIp = request.getHeader("X-Real-IP");
			if (StringUtils.hasText(xRealIp)) {
				return xRealIp;
			}

			return request.getRemoteAddr();
		}
	}
}