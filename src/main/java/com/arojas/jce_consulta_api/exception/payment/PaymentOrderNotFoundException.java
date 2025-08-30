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
 * Orden de pago no encontrada
 */
public class PaymentOrderNotFoundException extends PaymentException {
	public PaymentOrderNotFoundException(String paymentId) {
		super("Orden de pago no encontrada: " + paymentId);
	}
}
