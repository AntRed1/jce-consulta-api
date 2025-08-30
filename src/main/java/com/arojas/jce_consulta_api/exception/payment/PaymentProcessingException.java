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
 * Error de procesamiento de pago
 */
public class PaymentProcessingException extends PaymentException {
	public PaymentProcessingException(String paymentId, String message) {
		super("Error procesando pago " + paymentId + ": " + message);
	}

	public PaymentProcessingException(String paymentId, String message, Throwable cause) {
		super("Error procesando pago " + paymentId + ": " + message, cause);
	}
}
