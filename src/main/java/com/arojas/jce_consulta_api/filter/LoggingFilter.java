/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.arojas.jce_consulta_api.entity.LogEntry.LogLevel;
import com.arojas.jce_consulta_api.service.DbLoggerService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author arojas
 *         * Filtro para logging automático de requests HTTP
 *
 */

@Component
@Order(1)
@RequiredArgsConstructor
public class LoggingFilter implements Filter {

	private final DbLoggerService dbLoggerService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (!(request instanceof HttpServletRequest httpRequest) ||
				!(response instanceof HttpServletResponse httpResponse)) {
			chain.doFilter(request, response);
			return;
		}

		// Setup MDC for request tracking
		String correlationId = httpRequest.getHeader("X-Correlation-ID");
		if (correlationId == null) {
			correlationId = UUID.randomUUID().toString().substring(0, 8);
		}

		String requestId = UUID.randomUUID().toString().substring(0, 8);

		MDC.put("correlationId", correlationId);
		MDC.put("requestId", requestId);
		MDC.put("userEmail", httpRequest.getRemoteUser());

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		try {
			chain.doFilter(request, response);
			stopWatch.stop();

			// Log successful request
			logRequest(httpRequest, httpResponse, stopWatch.getTotalTimeMillis(), null);

		} catch (ServletException | IOException | IllegalStateException e) {
			stopWatch.stop();
			// Log failed request
			logRequest(httpRequest, httpResponse, stopWatch.getTotalTimeMillis(), e);
			throw e;

		} finally {
			// Clear MDC
			MDC.clear();
		}
	}

	private void logRequest(HttpServletRequest request, HttpServletResponse response,
			long executionTime, Exception exception) {

		String uri = request.getRequestURI();
		String method = request.getMethod();
		int statusCode = response.getStatus();

		LogLevel level = exception != null ? LogLevel.ERROR
				: statusCode >= 500 ? LogLevel.ERROR : statusCode >= 400 ? LogLevel.WARN : LogLevel.INFO;

		String message = String.format("%s %s - %d", method, uri, statusCode);

		dbLoggerService.log()
				.level(level)
				.source("HTTP_REQUEST")
				.operation(method + " " + uri)
				.message(message)
				.executionTime(executionTime)
				.withRequest(request)
				.context("httpMethod", method)
				.context("uri", uri)
				.context("statusCode", statusCode)
				.context("queryString", request.getQueryString())
				.context("contentType", request.getContentType())
				.exception(exception)
				.save();
	}
}
