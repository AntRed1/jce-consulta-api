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
 * Servicio JCE no disponible
 */
public class JceServiceUnavailableException extends CedulaQueryException {
	public JceServiceUnavailableException(String message) {
		super("Servicio JCE no disponible: " + message);
	}

	public JceServiceUnavailableException(String message, Throwable cause) {
		super("Servicio JCE no disponible: " + message, cause);
	}
}
