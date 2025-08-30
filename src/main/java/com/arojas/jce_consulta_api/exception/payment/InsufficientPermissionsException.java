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
 * Permisos insuficientes
 */
public class InsufficientPermissionsException extends PaymentException {
	public InsufficientPermissionsException(String operation) {
		super("Permisos insuficientes para: " + operation);
	}
}
