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
 * Error procesando consulta
 */
public class QueryProcessingException extends CedulaQueryException {
	public QueryProcessingException(String cedula, String message) {
		super(String.format("Error procesando consulta para cédula %s: %s", cedula, message));
	}

	public QueryProcessingException(String cedula, String message, Throwable cause) {
		super(String.format("Error procesando consulta para cédula %s: %s", cedula, message), cause);
	}
}
