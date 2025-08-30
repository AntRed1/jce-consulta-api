/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arojas.jce_consulta_api.dto.UserDto;
import com.arojas.jce_consulta_api.dto.response.ApiResponse;
import com.arojas.jce_consulta_api.dto.response.PaginatedResponse;
import com.arojas.jce_consulta_api.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author arojas
 */

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Users", description = "Gestión de usuarios")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

	private final UserService userService;

	@Operation(summary = "Obtener perfil del usuario actual", description = "Obtiene la información del perfil del usuario autenticado")
	@GetMapping("/profile")
	public ResponseEntity<ApiResponse<UserDto>> getUserProfile(Authentication authentication) {

		String userEmail = authentication.getName();
		log.info("Obteniendo perfil para usuario: {}", userEmail);

		try {
			UserDto user = userService.getUserByEmail(userEmail)
					.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

			ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
					.success(true)
					.data(user)
					.message("Perfil obtenido exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo perfil para {}: {}", userEmail, e.getMessage());

			ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo perfil")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Actualizar perfil del usuario", description = "Actualiza el nombre y email del usuario autenticado")
	@PutMapping("/profile")
	public ResponseEntity<ApiResponse<UserDto>> updateUserProfile(
			@RequestParam @NotBlank(message = "El nombre es requerido") @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres") String name,

			@RequestParam @NotBlank(message = "El email es requerido") @Email(message = "Email inválido") String email,

			Authentication authentication) {

		String userEmail = authentication.getName();
		log.info("Actualizando perfil para usuario: {}", userEmail);

		try {
			UserDto currentUser = userService.getUserByEmail(userEmail)
					.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

			UserDto updatedUser = userService.updateUserProfile(currentUser.getId(), name, email);

			ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
					.success(true)
					.data(updatedUser)
					.message("Perfil actualizado exitosamente")
					.build();

			log.info("Perfil actualizado exitosamente para usuario: {}", userEmail);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error actualizando perfil para {}: {}", userEmail, e.getMessage());

			ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
					.success(false)
					.data(null)
					.message("Error actualizando perfil")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Cambiar contraseña", description = "Permite al usuario cambiar su contraseña actual")
	@PostMapping("/change-password")
	public ResponseEntity<ApiResponse<String>> changePassword(
			@RequestParam @NotBlank(message = "La contraseña actual es requerida") String currentPassword,

			@RequestParam @NotBlank(message = "La nueva contraseña es requerida") @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres") String newPassword,

			@RequestParam @NotBlank(message = "La confirmación de contraseña es requerida") String confirmPassword,

			Authentication authentication) {

		String userEmail = authentication.getName();
		log.info("Solicitud de cambio de contraseña para usuario: {}", userEmail);

		try {
			// Validar que las contraseñas coincidan
			if (!newPassword.equals(confirmPassword)) {
				throw new IllegalArgumentException("Las contraseñas nuevas no coinciden");
			}

			UserDto currentUser = userService.getUserByEmail(userEmail)
					.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

			userService.changePassword(currentUser.getId(), currentPassword, newPassword);

			ApiResponse<String> response = ApiResponse.<String>builder()
					.success(true)
					.data("Contraseña cambiada exitosamente")
					.message("Tu contraseña ha sido actualizada correctamente")
					.build();

			log.info("Contraseña cambiada exitosamente para usuario: {}", userEmail);
			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException e) {
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

	@Operation(summary = "Obtener tokens disponibles", description = "Obtiene la cantidad de tokens disponibles del usuario")
	@GetMapping("/tokens")
	public ResponseEntity<ApiResponse<Integer>> getUserTokens(Authentication authentication) {

		String userEmail = authentication.getName();
		log.debug("Obteniendo tokens para usuario: {}", userEmail);

		try {
			UserDto user = userService.getUserByEmail(userEmail)
					.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

			ApiResponse<Integer> response = ApiResponse.<Integer>builder()
					.success(true)
					.data(user.getTokens())
					.message("Tokens obtenidos exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo tokens para {}: {}", userEmail, e.getMessage());

			ApiResponse<Integer> response = ApiResponse.<Integer>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo tokens")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	// ================= ENDPOINTS ADMINISTRATIVOS =================

	@Operation(summary = "Listar todos los usuarios (Admin)", description = "Obtiene una lista paginada de todos los usuarios del sistema")
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado - Se requieren permisos de administrador")
	})
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<PaginatedResponse<UserDto>>> getAllUsers(
			@Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") @Min(0) int page,

			@Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,

			@Parameter(description = "Campo para ordenar") @RequestParam(defaultValue = "name") String sortBy,

			@Parameter(description = "Dirección del ordenamiento") @RequestParam(defaultValue = "asc") @Pattern(regexp = "asc|desc") String sortDir) {

		log.info("Obteniendo lista de usuarios - página: {}, tamaño: {}", page, size);

		try {
			Page<UserDto> usersPage = userService.getAllUsers(page, size, sortBy, sortDir);

			PaginatedResponse<UserDto> paginatedResponse = PaginatedResponse.<UserDto>builder()
					.content(usersPage.getContent())
					.totalElements(usersPage.getTotalElements())
					.totalPages(usersPage.getTotalPages())
					.size(usersPage.getSize())
					.number(usersPage.getNumber())
					.build();

			ApiResponse<PaginatedResponse<UserDto>> response = ApiResponse.<PaginatedResponse<UserDto>>builder()
					.success(true)
					.data(paginatedResponse)
					.message("Usuarios obtenidos exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo lista de usuarios: {}", e.getMessage());

			ApiResponse<PaginatedResponse<UserDto>> response = ApiResponse.<PaginatedResponse<UserDto>>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo usuarios")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Buscar usuarios (Admin)", description = "Busca usuarios por nombre o email")
	@GetMapping("/search")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<PaginatedResponse<UserDto>>> searchUsers(
			@Parameter(description = "Término de búsqueda") @RequestParam @NotBlank(message = "El término de búsqueda es requerido") String term,

			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

		log.info("Buscando usuarios con término: {}", term);

		try {
			Page<UserDto> usersPage = userService.searchUsers(term, page, size);

			PaginatedResponse<UserDto> paginatedResponse = PaginatedResponse.<UserDto>builder()
					.content(usersPage.getContent())
					.totalElements(usersPage.getTotalElements())
					.totalPages(usersPage.getTotalPages())
					.size(usersPage.getSize())
					.number(usersPage.getNumber())
					.build();

			ApiResponse<PaginatedResponse<UserDto>> response = ApiResponse.<PaginatedResponse<UserDto>>builder()
					.success(true)
					.data(paginatedResponse)
					.message("Búsqueda completada exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error buscando usuarios: {}", e.getMessage());

			ApiResponse<PaginatedResponse<UserDto>> response = ApiResponse.<PaginatedResponse<UserDto>>builder()
					.success(false)
					.data(null)
					.message("Error en la búsqueda")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Obtener usuario por ID (Admin)", description = "Obtiene un usuario específico por su ID")
	@GetMapping("/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<UserDto>> getUserById(
			@Parameter(description = "ID del usuario") @PathVariable @NotBlank String userId) {

		log.info("Obteniendo usuario por ID: {}", userId);

		try {
			UserDto user = userService.getUserById(userId)
					.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

			ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
					.success(true)
					.data(user)
					.message("Usuario obtenido exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo usuario {}: {}", userId, e.getMessage());

			ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo usuario")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Activar/Desactivar usuario (Admin)", description = "Cambia el estado activo/inactivo de un usuario")
	@PutMapping("/{userId}/toggle-status")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<UserDto>> toggleUserStatus(
			@Parameter(description = "ID del usuario") @PathVariable @NotBlank String userId,
			Authentication authentication) {

		String adminEmail = authentication.getName();
		log.info("Admin {} cambiando estado de usuario: {}", adminEmail, userId);

		try {
			UserDto admin = userService.getUserByEmail(adminEmail)
					.orElseThrow(() -> new RuntimeException("Usuario administrador no encontrado"));

			UserDto updatedUser = userService.toggleUserStatus(userId, admin.getId());

			ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
					.success(true)
					.data(updatedUser)
					.message("Estado del usuario actualizado exitosamente")
					.build();

			log.info("Estado de usuario {} actualizado por admin {}", userId, adminEmail);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error cambiando estado de usuario {}: {}", userId, e.getMessage());

			ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
					.success(false)
					.data(null)
					.message("Error cambiando estado del usuario")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Establecer tokens a usuario (Admin)", description = "Establece manualmente la cantidad de tokens de un usuario")
	@PutMapping("/{userId}/tokens")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<UserDto>> setUserTokens(
			@Parameter(description = "ID del usuario") @PathVariable @NotBlank String userId,

			@Parameter(description = "Cantidad de tokens") @RequestParam @Min(0) @Max(1000) int tokens,

			Authentication authentication) {

		String adminEmail = authentication.getName();
		log.info("Admin {} estableciendo {} tokens para usuario: {}", adminEmail, tokens, userId);

		try {
			UserDto admin = userService.getUserByEmail(adminEmail)
					.orElseThrow(() -> new RuntimeException("Usuario administrador no encontrado"));

			UserDto updatedUser = userService.setUserTokens(userId, tokens, admin.getId());

			ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
					.success(true)
					.data(updatedUser)
					.message("Tokens establecidos exitosamente")
					.build();

			log.info("Tokens establecidos exitosamente para usuario {} por admin {}", userId, adminEmail);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error estableciendo tokens para usuario {}: {}", userId, e.getMessage());

			ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
					.success(false)
					.data(null)
					.message("Error estableciendo tokens")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Eliminar usuario (Admin)", description = "Desactiva permanentemente un usuario del sistema")
	@DeleteMapping("/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<String>> deleteUser(
			@Parameter(description = "ID del usuario") @PathVariable @NotBlank String userId,
			Authentication authentication) {

		String adminEmail = authentication.getName();
		log.info("Admin {} eliminando usuario: {}", adminEmail, userId);

		try {
			UserDto admin = userService.getUserByEmail(adminEmail)
					.orElseThrow(() -> new RuntimeException("Usuario administrador no encontrado"));

			userService.deleteUser(userId, admin.getId());

			ApiResponse<String> response = ApiResponse.<String>builder()
					.success(true)
					.data("Usuario eliminado exitosamente")
					.message("El usuario ha sido desactivado permanentemente")
					.build();

			log.info("Usuario {} eliminado exitosamente por admin {}", userId, adminEmail);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error eliminando usuario {}: {}", userId, e.getMessage());

			ApiResponse<String> response = ApiResponse.<String>builder()
					.success(false)
					.data(null)
					.message("Error eliminando usuario")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Obtener estadísticas de usuarios (Admin)", description = "Obtiene estadísticas generales del sistema de usuarios")
	@GetMapping("/stats")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<UserService.UserStatsDto>> getUserStatistics() {

		log.info("Obteniendo estadísticas de usuarios");

		try {
			UserService.UserStatsDto stats = userService.getUserStatistics();

			ApiResponse<UserService.UserStatsDto> response = ApiResponse.<UserService.UserStatsDto>builder()
					.success(true)
					.data(stats)
					.message("Estadísticas obtenidas exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo estadísticas de usuarios: {}", e.getMessage());

			ApiResponse<UserService.UserStatsDto> response = ApiResponse.<UserService.UserStatsDto>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo estadísticas")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Obtener usuarios con tokens expirados (Admin)", description = "Lista usuarios que tienen tokens expirados que requieren limpieza")
	@GetMapping("/expired-tokens")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<java.util.List<UserDto>>> getUsersWithExpiredTokens() {

		log.info("Obteniendo usuarios con tokens expirados");

		try {
			java.util.List<UserDto> users = userService.getUsersWithExpiredTokens();

			ApiResponse<java.util.List<UserDto>> response = ApiResponse.<java.util.List<UserDto>>builder()
					.success(true)
					.data(users)
					.message("Usuarios con tokens expirados obtenidos exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo usuarios con tokens expirados: {}", e.getMessage());

			ApiResponse<java.util.List<UserDto>> response = ApiResponse.<java.util.List<UserDto>>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo usuarios con tokens expirados")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}
}