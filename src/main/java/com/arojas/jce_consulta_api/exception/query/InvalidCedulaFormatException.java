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
 * Formato de cédula inválido
 */
public class InvalidCedulaFormatException extends CedulaQueryException {
	public InvalidCedulaFormatException(String cedula, String reason) {
		super(String.format("Formato de cédula inválido '%s': %s", cedula, reason));
	}
}
