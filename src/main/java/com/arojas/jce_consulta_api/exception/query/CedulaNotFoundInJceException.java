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
 * Cédula no encontrada en JCE
 */
public class CedulaNotFoundInJceException extends CedulaQueryException {
	public CedulaNotFoundInJceException(String cedula) {
		super("Cédula no encontrada en la base de datos de JCE: " + cedula);
	}
}