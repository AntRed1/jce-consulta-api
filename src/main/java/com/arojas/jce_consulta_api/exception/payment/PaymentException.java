/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.exception.payment;

/**
 *
 * @author arojas
 */
public abstract class PaymentException extends RuntimeException {

	protected PaymentException(String message) {
		super(message);
	}

	protected PaymentException(String message, Throwable cause) {
		super(message, cause);
	}
}
