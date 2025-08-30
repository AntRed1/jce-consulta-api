package com.arojas.jce_consulta_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class LoginCredentials {

	@Email(message = "Email debe tener un formato válido")
	@NotBlank(message = "Email es requerido")
	private String email;

	@NotBlank(message = "Contraseña es requerida")
	@Size(min = 6, message = "Contraseña debe tener al menos 6 caracteres")
	private String password;
}
