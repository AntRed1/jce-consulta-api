/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.exception.payment;

/**
 *
 * @author arojas
 */
/**
 * Factory para crear excepciones de pago de manera consistente
 */
public class PaymentExceptions {

	private PaymentExceptions() {
	}

	public static UserNotFoundException userNotFound(String userId) {
		return new UserNotFoundException(userId);
	}

	public static InactiveUserException userInactive(String userId) {
		return new InactiveUserException(userId);
	}

	public static PaymentOrderNotFoundException paymentNotFound(String paymentId) {
		return new PaymentOrderNotFoundException(paymentId);
	}

	public static InvalidPaymentStatusException invalidStatus(String expected, String actual) {
		return new InvalidPaymentStatusException(expected, actual);
	}

	public static InvalidTokenQuantityException invalidTokenQuantity(int quantity) {
		return new InvalidTokenQuantityException(quantity);
	}

	public static PaymentProcessingException processingError(String paymentId, String message) {
		return new PaymentProcessingException(paymentId, message);
	}

	public static PaymentProcessingException processingError(String paymentId, String message, Throwable cause) {
		return new PaymentProcessingException(paymentId, message, cause);
	}

	public static InsufficientPermissionsException insufficientPermissions(String operation) {
		return new InsufficientPermissionsException(operation);
	}

	public static InvalidWebhookException invalidWebhook(String reason) {
		return new InvalidWebhookException(reason);
	}
}
