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
 * Webhook inválido
 */
public class InvalidWebhookException extends PaymentException {
	public InvalidWebhookException(String reason) {
		super("Webhook inválido: " + reason);
	}
}