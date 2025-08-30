package com.arojas.jce_consulta_api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author arojas
 */

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
	private final boolean success;
	private final T data;
	private final String message;
	private final String error;

	// Factory methods para crear respuestas de éxito
	public static <T> ApiResponse<T> success(T data) {
		return ApiResponse.<T>builder()
				.success(true)
				.data(data)
				.build();
	}

	public static <T> ApiResponse<T> success(T data, String message) {
		return ApiResponse.<T>builder()
				.success(true)
				.data(data)
				.message(message)
				.build();
	}

	// Factory methods para crear respuestas de error
	public static <T> ApiResponse<T> error(String error) {
		return ApiResponse.<T>builder()
				.success(false)
				.error(error)
				.build();
	}

	public static <T> ApiResponse<T> error(String message, String error) {
		return ApiResponse.<T>builder()
				.success(false)
				.message(message)
				.error(error)
				.build();
	}
}
