/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.exception.query;

/**
 *
 * @author arojas
 *         * Excepción base para operaciones de consulta de cédula
 *         * Permite manejar errores específicos relacionados con consultas de
 *         cédula
 */
public abstract class CedulaQueryException extends RuntimeException {

	protected CedulaQueryException(String message) {
		super(message);
	}

	protected CedulaQueryException(String message, Throwable cause) {
		super(message, cause);
	}
}
