package com.arojas.jce_consulta_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arojas.jce_consulta_api.dto.request.LoginCredentials;
import com.arojas.jce_consulta_api.dto.request.RegisterData;
import com.arojas.jce_consulta_api.dto.response.ApiResponse;
import com.arojas.jce_consulta_api.dto.response.AuthResponse;
import com.arojas.jce_consulta_api.service.AuthService;
import com.arojas.jce_consulta_api.entity.User;
import com.arojas.jce_consulta_api.dto.UserDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author arojas
 */

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Authentication", description = "Endpoints para autenticación y autorización")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

	private final AuthService authService;

	@Operation(summary = "Registro de usuario", description = "Registra un nuevo usuario en el sistema con rol USER por defecto")
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de registro inválidos"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email ya registrado")
	})
	@PostMapping("/register")
	public ResponseEntity<ApiResponse<AuthResponse>> register(
			@Valid @RequestBody RegisterData registerData,
			HttpServletRequest request) {

		log.info("Solicitud de registro para email: {}", registerData.getEmail());

		try {
			// Obtener IP del cliente (para logging, pero no se pasa al service)
			String clientIp = getClientIpAddress(request);
			log.info("Registro desde IP: {}", clientIp);

			// Registrar usuario - solo pasa RegisterData
			AuthResponse authResponse = authService.register(registerData);

			ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
					.success(true)
					.data(authResponse)
					.message("Usuario registrado exitosamente")
					.build();

			log.info("Usuario registrado exitosamente: {}", registerData.getEmail());
			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (Exception e) {
			log.error("Error en registro para {}: {}", registerData.getEmail(), e.getMessage());

			ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
					.success(false)
					.data(null)
					.message("Error en el registro")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Inicio de sesión", description = "Autentica un usuario y devuelve tokens de acceso")
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login exitoso", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "423", description = "Usuario inactivo")
	})
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<AuthResponse>> login(
			@Valid @RequestBody LoginCredentials credentials,
			HttpServletRequest request) {

		log.info("Solicitud de login para email: {}", credentials.getEmail());

		try {
			// Obtener IP real del cliente
			String clientIp = getClientIpAddress(request);
			// Capturar User-Agent del navegador/dispositivo
			String deviceInfo = request.getHeader("User-Agent");

			log.info("Login desde IP: {}, dispositivo: {}", clientIp, deviceInfo);

			// Llamada al AuthService pasando IP y dispositivo
			AuthResponse authResponse = authService.login(credentials, clientIp, deviceInfo);

			ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
					.success(true)
					.data(authResponse)
					.message("Login exitoso")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error en login para {}: {}", credentials.getEmail(), e.getMessage());

			ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
					.success(false)
					.data(null)
					.message("Error en el login")
					.error(e.getMessage())
					.build();

			HttpStatus status = e.getMessage().contains("inactivo") ? HttpStatus.LOCKED : HttpStatus.UNAUTHORIZED;

			return ResponseEntity.status(status).body(response);
		}
	}

	@Operation(summary = "Refrescar token", description = "Genera un nuevo token de acceso usando el refresh token")
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refrescado exitosamente"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
	})
	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
			@RequestParam String refreshToken,
			HttpServletRequest request) {

		log.info("Solicitud de refresh token");

		try {
			String clientIp = getClientIpAddress(request);
			log.info("Refresh token desde IP: {}", clientIp);

			// Solo pasa el refreshToken (String)
			AuthResponse authResponse = authService.refreshToken(refreshToken);

			ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
					.success(true)
					.data(authResponse)
					.message("Token refrescado exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error refrescando token: {}", e.getMessage());

			ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
					.success(false)
					.data(null)
					.message("Error refrescando token")
					.error(e.getMessage())
					.build();

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
	}

	@Operation(summary = "Cerrar sesión", description = "Invalida el refresh token del usuario")
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout exitoso")
	})
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<String>> logout(@RequestParam String refreshToken) {

		log.info("Solicitud de logout");

		try {
			// Usa el refreshToken para el logout
			authService.logout(refreshToken);

			ApiResponse<String> response = ApiResponse.<String>builder()
					.success(true)
					.data("Logout exitoso")
					.message("Sesión cerrada correctamente")
					.build();

			log.info("Logout exitoso");
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error en logout: {}", e.getMessage());

			ApiResponse<String> response = ApiResponse.<String>builder()
					.success(false)
					.data(null)
					.message("Error cerrando sesión")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Validar token", description = "Valida si el token actual es válido y devuelve información del usuario")
	@GetMapping("/validate")
	public ResponseEntity<ApiResponse<AuthResponse>> validateToken(Authentication authentication) {

		if (authentication != null) {
			String userEmail = authentication.getName();
			log.debug("Validando token para: {}", userEmail);

			try {
				// Crear AuthResponse manualmente usando el email del usuario autenticado
				User user = authService.getUserFromToken(extractTokenFromAuthentication(authentication));
				UserDto userDto = UserDto.fromEntity(user);

				AuthResponse authResponse = AuthResponse.builder()
						.user(userDto)
						.build();

				ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
						.success(true)
						.data(authResponse)
						.message("Token válido")
						.build();

				return ResponseEntity.ok(response);

			} catch (Exception e) {
				log.error("Error validando token para {}: {}", userEmail, e.getMessage());

				ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
						.success(false)
						.data(null)
						.message("Token inválido")
						.error(e.getMessage())
						.build();

				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}
		}

		ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
				.success(false)
				.data(null)
				.message("Token no proporcionado")
				.error("No authenticated")
				.build();

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	@Operation(summary = "Cambiar contraseña", description = "Permite al usuario autenticado cambiar su contraseña")
	@PostMapping("/change-password")
	public ResponseEntity<ApiResponse<String>> changePassword(
			@RequestParam String currentPassword,
			@RequestParam String newPassword,
			@RequestParam String confirmPassword,
			Authentication authentication) {

		String userEmail = authentication.getName();
		log.info("Solicitud de cambio de contraseña para: {}", userEmail);

		try {
			// Validar que las contraseñas nuevas coincidan
			if (!newPassword.equals(confirmPassword)) {
				throw new IllegalArgumentException("Las contraseñas nuevas no coinciden");
			}

			authService.changePassword(userEmail, currentPassword, newPassword);

			ApiResponse<String> response = ApiResponse.<String>builder()
					.success(true)
					.data("Contraseña cambiada exitosamente")
					.message("Tu contraseña ha sido actualizada")
					.build();

			log.info("Contraseña cambiada exitosamente para: {}", userEmail);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error cambiando contraseña para {}: {}", userEmail, e.getMessage());

			ApiResponse<String> response = ApiResponse.<String>builder()
					.success(false)
					.data(null)
					.message("Error cambiando contraseña")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Información del usuario actual", description = "Obtiene la información del usuario autenticado")
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser(Authentication authentication) {

		String userEmail = authentication.getName();
		log.debug("Obteniendo información del usuario actual: {}", userEmail);

		try {
			// Crear AuthResponse manualmente usando el email del usuario autenticado
			User user = authService.getUserFromToken(extractTokenFromAuthentication(authentication));
			UserDto userDto = UserDto.fromEntity(user);

			AuthResponse authResponse = AuthResponse.builder()
					.user(userDto)
					.build();

			ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
					.success(true)
					.data(authResponse)
					.message("Información del usuario obtenida")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo información del usuario {}: {}", userEmail, e.getMessage());

			ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo información del usuario")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	// ================= MÉTODOS PRIVADOS =================

	private String getClientIpAddress(HttpServletRequest request) {
		String xForwardedForHeader = request.getHeader("X-Forwarded-For");
		if (xForwardedForHeader == null) {
			return request.getRemoteAddr();
		} else {
			return xForwardedForHeader.split(",")[0].trim();
		}
	}

	private String extractTokenFromAuthentication(Authentication authentication) {
		// Para estos endpoints, simplemente buscaremos el usuario por email
		// ya que no necesitamos el token JWT en sí, solo la información del usuario
		return authentication.getName();
	}
}