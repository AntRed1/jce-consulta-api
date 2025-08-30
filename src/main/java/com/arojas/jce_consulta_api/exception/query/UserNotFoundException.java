/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.exception.query;

/**
 *
 * @author arojas
 */

/**
 * Usuario no encontrado
 */
public class UserNotFoundException extends CedulaQueryException {
	public UserNotFoundException(String userEmail) {
		super("Usuario no encontrado: " + userEmail);
	}
}
