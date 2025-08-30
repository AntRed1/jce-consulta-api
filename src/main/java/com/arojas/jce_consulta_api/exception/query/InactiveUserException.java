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
 * Usuario inactivo
 */
public class InactiveUserException extends CedulaQueryException {
	public InactiveUserException(String userEmail) {
		super("Usuario inactivo: " + userEmail);
	}
}