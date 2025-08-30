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
 * Usuario no tiene tokens suficientes
 */
public class InsufficientTokensException extends CedulaQueryException {
	public InsufficientTokensException(String userId) {
		super("No tienes tokens disponibles para realizar consultas");
	}

	public InsufficientTokensException(String userId, int available, int required) {
		super(String.format("Tokens insuficientes. Disponibles: %d, Requeridos: %d", available, required));
	}
}