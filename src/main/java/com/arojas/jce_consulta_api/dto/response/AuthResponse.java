package com.arojas.jce_consulta_api.dto.response;

import com.arojas.jce_consulta_api.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class AuthResponse {

	private String token;
	private UserDto user;

	@JsonProperty("refreshToken")
	private String refreshToken;
}
