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
 * Estado de pago inválido
 */
public class InvalidPaymentStatusException extends PaymentException {
	public InvalidPaymentStatusException(String expectedStatus, String actualStatus) {
		super(String.format("Estado de pago inválido. Esperado: %s, Actual: %s",
				expectedStatus, actualStatus));
	}
}