package com.arojas.jce_consulta_api.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.arojas.jce_consulta_api.dto.response.ApiResponse;
import com.arojas.jce_consulta_api.exception.payment.InactiveUserException;
import com.arojas.jce_consulta_api.exception.payment.InsufficientPermissionsException;
import com.arojas.jce_consulta_api.exception.payment.InvalidPaymentStatusException;
import com.arojas.jce_consulta_api.exception.payment.InvalidTokenQuantityException;
import com.arojas.jce_consulta_api.exception.payment.InvalidWebhookException;
import com.arojas.jce_consulta_api.exception.payment.PaymentOrderNotFoundException;
import com.arojas.jce_consulta_api.exception.payment.PaymentProcessingException;
import com.arojas.jce_consulta_api.exception.payment.UserNotFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * Manejador centralizado de excepciones para operaciones de pago
 * Convierte excepciones específicas en respuestas HTTP apropiadas
 */
@ControllerAdvice
@Slf4j
public class PaymentExceptionHandler {

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

	@ExceptionHandler(PaymentOrderNotFoundException.class)
	public ResponseEntity<ApiResponse<Object>> handlePaymentOrderNotFound(PaymentOrderNotFoundException ex) {
		log.warn("Payment order not found: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiResponse.error("Orden de pago no encontrada", ex.getMessage()));
	}

	@ExceptionHandler(InvalidPaymentStatusException.class)
	public ResponseEntity<ApiResponse<Object>> handleInvalidPaymentStatus(InvalidPaymentStatusException ex) {
		log.warn("Invalid payment status: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.error("Estado de pago inválido", ex.getMessage()));
	}

	@ExceptionHandler(InvalidTokenQuantityException.class)
	public ResponseEntity<ApiResponse<Object>> handleInvalidTokenQuantity(InvalidTokenQuantityException ex) {
		log.warn("Invalid token quantity: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.error("Cantidad de tokens inválida", ex.getMessage()));
	}

	@ExceptionHandler(PaymentProcessingException.class)
	public ResponseEntity<ApiResponse<Object>> handlePaymentProcessing(PaymentProcessingException ex) {
		log.error("Payment processing error: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.error("Error procesando pago", ex.getMessage()));
	}

	@ExceptionHandler(InsufficientPermissionsException.class)
	public ResponseEntity<ApiResponse<Object>> handleInsufficientPermissions(InsufficientPermissionsException ex) {
		log.warn("Insufficient permissions: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(ApiResponse.error("Permisos insuficientes", ex.getMessage()));
	}

	@ExceptionHandler(InvalidWebhookException.class)
	public ResponseEntity<ApiResponse<Object>> handleInvalidWebhook(InvalidWebhookException ex) {
		log.warn("Invalid webhook: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponse.error("Webhook inválido", ex.getMessage()));
	}
}