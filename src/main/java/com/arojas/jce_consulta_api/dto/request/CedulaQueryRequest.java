package com.arojas.jce_consulta_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author arojas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CedulaQueryRequest {

	@NotBlank(message = "Cédula es requerida")
	@Pattern(regexp = "^\\d{11}$|^\\d{3}-\\d{7}-\\d{1}$", message = "Formato de cédula inválido. Use: 00000000000 o 000-0000000-0")
	private String cedula;
}
