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
 * Cantidad de tokens inválida
 */
public class InvalidTokenQuantityException extends PaymentException {
	public InvalidTokenQuantityException(int quantity) {
		super("Cantidad de tokens inválida: " + quantity + ". Debe ser mayor a 0");
	}
}