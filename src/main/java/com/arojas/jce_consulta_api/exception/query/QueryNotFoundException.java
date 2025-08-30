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
 * Consulta no encontrada
 */
public class QueryNotFoundException extends CedulaQueryException {
	public QueryNotFoundException(String queryId) {
		super("Consulta no encontrada: " + queryId);
	}
}
