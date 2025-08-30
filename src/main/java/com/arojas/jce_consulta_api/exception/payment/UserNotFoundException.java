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
 * Usuario no encontrado
 */
public class UserNotFoundException extends PaymentException {
	public UserNotFoundException(String userId) {
		super("Usuario no encontrado: " + userId);
	}
}