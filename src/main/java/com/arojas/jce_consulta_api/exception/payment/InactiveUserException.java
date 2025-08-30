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
 * Usuario inactivo
 */
public class InactiveUserException extends PaymentException {
	public InactiveUserException(String userId) {
		super("Usuario inactivo: " + userId);
	}
}