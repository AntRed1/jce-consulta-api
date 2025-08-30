/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.arojas.jce_consulta_api.dto.response.ApiResponse;
import com.arojas.jce_consulta_api.exception.payment.UserNotFoundException;
import com.arojas.jce_consulta_api.exception.query.CedulaNotFoundInJceException;
import com.arojas.jce_consulta_api.exception.query.InactiveUserException;
import com.arojas.jce_consulta_api.exception.query.InsufficientTokensException;
import com.arojas.jce_consulta_api.exception.query.InvalidCedulaFormatException;
import com.arojas.jce_consulta_api.exception.query.JceServiceUnavailableException;
import com.arojas.jce_consulta_api.exception.query.QueryLimitExceededException;
import com.arojas.jce_consulta_api.exception.query.QueryNotFoundException;
import com.arojas.jce_consulta_api.exception.query.QueryProcessingException;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author arojas
 *         Manejador centralizado de excepciones para consultas de cédula
 *         Convierte excepciones específicas en respuestas HTTP apropiadas
 */

@ControllerAdvice
@Slf4j
public class CedulaQueryExceptionHandler {

	@ExceptionHandler(InvalidCedulaFormatException.class)
	public ResponseEntity<ApiResponse<Object>> handleInvalidCedulaFormat(InvalidCedulaFormatException ex) {
		log.warn("Invalid cedula format: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.error("Formato de cédula inválido", ex.getMessage()));
	}

	@ExceptionHandler(InsufficientTokensException.class)
	public ResponseEntity<ApiResponse<Object>> handleInsufficientTokens(InsufficientTokensException ex) {
		log.warn("Insufficient tokens: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED) // 402
				.body(ApiResponse.error("Tokens insuficientes", ex.getMessage()));
	}

	@ExceptionHandler(QueryNotFoundException.class)
	public ResponseEntity<ApiResponse<Object>> handleQueryNotFound(QueryNotFoundException ex) {
		log.warn("Query not found: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiResponse.error("Consulta no encontrada", ex.getMessage()));
	}

	@ExceptionHandler(JceServiceUnavailableException.class)
	public ResponseEntity<ApiResponse<Object>> handleJceServiceUnavailable(JceServiceUnavailableException ex) {
		log.error("JCE service unavailable: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE) // 503
				.body(ApiResponse.error("Servicio JCE no disponible", ex.getMessage()));
	}

	@ExceptionHandler(QueryProcessingException.class)
	public ResponseEntity<ApiResponse<Object>> handleQueryProcessing(QueryProcessingException ex) {
		log.error("Query processing error: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.error("Error procesando consulta", ex.getMessage()));
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ApiResponse<Object>> handleUserNotFound(UserNotFoundException ex) {
		log.warn("User not found: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiResponse.error("Usuario no encontrado", ex.getMessage()));
	}

	@ExceptionHandler(InactiveUserException.class)
	public ResponseEntity<ApiResponse<Object>> handleInactiveUser(InactiveUserException ex) {
		log.warn("Inactive user access attempt: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(ApiResponse.error("Usuario inactivo", ex.getMessage()));
	}

	@ExceptionHandler(CedulaNotFoundInJceException.class)
	public ResponseEntity<ApiResponse<Object>> handleCedulaNotFoundInJce(CedulaNotFoundInJceException ex) {
		log.info("Cedula not found in JCE: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiResponse.error("Cédula no encontrada", ex.getMessage()));
	}

	@ExceptionHandler(QueryLimitExceededException.class)
	public ResponseEntity<ApiResponse<Object>> handleQueryLimitExceeded(QueryLimitExceededException ex) {
		log.warn("Query limit exceeded: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS) // 429
				.body(ApiResponse.error("Límite de consultas excedido", ex.getMessage()));
	}
}