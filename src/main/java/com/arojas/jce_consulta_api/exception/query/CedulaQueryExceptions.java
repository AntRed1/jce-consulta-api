/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.exception.query;

/**
 *
 * @author arojas
 *         * Factory para crear excepciones de consulta de manera consistente
 *         * Proporciona métodos estáticos para instanciar diferentes tipos de
 *         excepciones de consulta de cédula
 */

public class CedulaQueryExceptions {

	private CedulaQueryExceptions() {
	}

	public static InvalidCedulaFormatException invalidFormat(String cedula, String reason) {
		return new InvalidCedulaFormatException(cedula, reason);
	}

	public static InsufficientTokensException insufficientTokens(String userId) {
		return new InsufficientTokensException(userId);
	}

	public static InsufficientTokensException insufficientTokens(String userId, int available, int required) {
		return new InsufficientTokensException(userId, available, required);
	}

	public static QueryNotFoundException queryNotFound(String queryId) {
		return new QueryNotFoundException(queryId);
	}

	public static JceServiceUnavailableException jceUnavailable(String message) {
		return new JceServiceUnavailableException(message);
	}

	public static JceServiceUnavailableException jceUnavailable(String message, Throwable cause) {
		return new JceServiceUnavailableException(message, cause);
	}

	public static QueryProcessingException processingError(String cedula, String message) {
		return new QueryProcessingException(cedula, message);
	}

	public static QueryProcessingException processingError(String cedula, String message, Throwable cause) {
		return new QueryProcessingException(cedula, message, cause);
	}

	public static UserNotFoundException userNotFound(String userEmail) {
		return new UserNotFoundException(userEmail);
	}

	public static InactiveUserException userInactive(String userEmail) {
		return new InactiveUserException(userEmail);
	}

	public static CedulaNotFoundInJceException cedulaNotFoundInJce(String cedula) {
		return new CedulaNotFoundInJceException(cedula);
	}

	public static QueryLimitExceededException limitExceeded(String period, int limit) {
		return new QueryLimitExceededException(period, limit);
	}
}
