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
 * Límite de consultas excedido
 */
public class QueryLimitExceededException extends CedulaQueryException {
	public QueryLimitExceededException(String period, int limit) {
		super(String.format("Límite de consultas excedido para el período %s. Límite: %d", period, limit));
	}
}