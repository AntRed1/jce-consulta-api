package com.arojas.jce_consulta_api.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta paginada genérica para listas.
 *
 * @param <T> Tipo del contenido de la lista
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {

	private List<T> content;
	private long totalElements;
	private int totalPages;
	private int size;
	private int number;

	/**
	 * Crea un PaginatedResponse a partir de un Page de Spring Data
	 */
	public static <T> PaginatedResponse<T> from(Page<T> page) {
		return new PaginatedResponse<>(
				page.getContent(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.getSize(),
				page.getNumber());
	}
}
