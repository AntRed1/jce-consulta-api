/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arojas.jce_consulta_api.dto.UserDto;
import com.arojas.jce_consulta_api.entity.User;
import com.arojas.jce_consulta_api.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author arojas
 */

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;

	private static final int TOKEN_EXPIRY_HOURS = 24;

	/**
	 * Obtiene un usuario por ID
	 */
	@Transactional(readOnly = true)
	@Cacheable(value = "users", key = "#userId")
	public Optional<UserDto> getUserById(String userId) {
		log.debug("Obteniendo usuario por ID: {}", userId);
		return userRepository.findById(userId).map(this::convertToDto);
	}

	/**
	 * Obtiene un usuario por email
	 */
	@Transactional(readOnly = true)
	public Optional<UserDto> getUserByEmail(String email) {
		log.debug("Obteniendo usuario por email: {}", email);
		return userRepository.findByEmail(email).map(this::convertToDto);
	}

	/**
	 * Obtiene todos los usuarios con paginación
	 */
	@Transactional(readOnly = true)
	public Page<UserDto> getAllUsers(int page, int size, String sortBy, String sortDir) {
		log.info("Obteniendo usuarios - página: {}, tamaño: {}, ordenar por: {}", page, size, sortBy);

		Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
		Pageable pageable = PageRequest.of(page, size, sort);

		Page<User> users = userRepository.findAll(pageable);
		return users.map(this::convertToDto);
	}

	/**
	 * Busca usuarios por nombre o email
	 */
	@Transactional(readOnly = true)
	public Page<UserDto> searchUsers(String searchTerm, int page, int size) {
		log.info("Buscando usuarios con término: {}", searchTerm);

		Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
		Page<User> users = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
				searchTerm, searchTerm, pageable);

		return users.map(this::convertToDto);
	}

	/**
	 * Actualiza perfil de usuario
	 */
	@CacheEvict(value = "users", key = "#userId")
	public UserDto updateUserProfile(String userId, String name, String email) {
		log.info("Actualizando perfil de usuario: {}", userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		// Verificar si el email ya existe (si es diferente al actual)
		if (!user.getEmail().equals(email)) {
			Optional<User> existingUser = userRepository.findByEmail(email);
			if (existingUser.isPresent()) {
				throw new RuntimeException("El email ya está en uso");
			}
		}

		user.setName(name);
		user.setEmail(email);

		user = userRepository.save(user);

		return convertToDto(user);
	}

	/**
	 * Cambia contraseña de usuario
	 */
	@CacheEvict(value = "users", key = "#userId")
	public void changePassword(String userId, String currentPassword, String newPassword) {
		log.info("Cambiando contraseña para usuario: {}", userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		// Verificar contraseña actual
		if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
			throw new RuntimeException("Contraseña actual incorrecta");
		}

		// Validar nueva contraseña
		validatePassword(newPassword);

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		log.info("Contraseña cambiada exitosamente para usuario: {}", userId);
	}

	/**
	 * Activa o desactiva un usuario (solo admin)
	 */
	@CacheEvict(value = "users", key = "#userId")
	public UserDto toggleUserStatus(String userId, String adminUserId) {
		log.info("Cambiando estado de usuario: {} por admin: {}", userId, adminUserId);

		// Validar que el admin existe y tiene permisos
		User admin = userRepository.findById(adminUserId)
				.orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

		if (!admin.getRole().equals(User.Role.ADMIN)) {
			throw new RuntimeException("Solo los administradores pueden cambiar el estado de usuarios");
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		// No permitir desactivar al propio admin
		if (userId.equals(adminUserId)) {
			throw new RuntimeException("No puedes desactivar tu propia cuenta");
		}

		user.setIsActive(!user.getIsActive());
		user = userRepository.save(user);

		log.info("Estado de usuario {} cambiado a: {}", userId, user.getIsActive() ? "activo" : "inactivo");

		return convertToDto(user);
	}

	/**
	 * Agrega tokens a un usuario
	 */
	@CacheEvict(value = "users", key = "#userId")
	@Transactional
	public UserDto addTokens(String userId, int tokensToAdd) {
		log.info("Agregando {} tokens al usuario: {}", tokensToAdd, userId);

		if (tokensToAdd <= 0) {
			throw new IllegalArgumentException("La cantidad de tokens debe ser mayor a 0");
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		int newTokenCount = user.getTokens() + tokensToAdd;
		user.setTokens(newTokenCount);
		user.setLastTokenUpdate(LocalDateTime.now());

		user = userRepository.save(user);

		log.info("Tokens agregados exitosamente. Usuario {} ahora tiene {} tokens", userId, newTokenCount);

		return convertToDto(user);
	}

	/**
	 * Consume un token del usuario
	 */
	@CacheEvict(value = "users", key = "#userId")
	@Transactional
	public UserDto consumeToken(String userId) {
		log.info("Consumiendo token del usuario: {}", userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		if (!user.getIsActive()) {
			throw new RuntimeException("Usuario inactivo");
		}

		if (user.getTokens() <= 0) {
			throw new RuntimeException("No tienes tokens disponibles");
		}

		// Verificar si los tokens han expirado (24 horas)
		if (user.getLastTokenUpdate() != null &&
				user.getLastTokenUpdate().isBefore(LocalDateTime.now().minusHours(TOKEN_EXPIRY_HOURS))) {

			log.info("Tokens expirados para usuario: {}, limpiando tokens", userId);
			user.setTokens(0);
			userRepository.save(user);
			throw new RuntimeException("Tus tokens han expirado");
		}

		user.setTokens(user.getTokens() - 1);
		user = userRepository.save(user);

		log.info("Token consumido. Usuario {} ahora tiene {} tokens", userId, user.getTokens());

		return convertToDto(user);
	}

	/**
	 * Establece tokens manualmente (solo admin)
	 */
	@CacheEvict(value = "users", key = "#userId")
	public UserDto setUserTokens(String userId, int tokenCount, String adminUserId) {
		log.info("Estableciendo {} tokens para usuario: {} por admin: {}", tokenCount, userId, adminUserId);

		// Validar admin
		User admin = userRepository.findById(adminUserId)
				.orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

		if (!admin.getRole().equals(User.Role.ADMIN)) {
			throw new RuntimeException("Solo los administradores pueden establecer tokens manualmente");
		}

		if (tokenCount < 0) {
			throw new IllegalArgumentException("La cantidad de tokens no puede ser negativa");
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		user.setTokens(tokenCount);
		user.setLastTokenUpdate(LocalDateTime.now());

		user = userRepository.save(user);

		log.info("Tokens establecidos exitosamente. Usuario {} ahora tiene {} tokens", userId, tokenCount);

		return convertToDto(user);
	}

	/**
	 * Obtiene estadísticas de usuarios
	 */
	@Transactional(readOnly = true)
	public UserStatsDto getUserStatistics() {
		log.info("Obteniendo estadísticas de usuarios");

		long totalUsers = userRepository.count();
		long activeUsers = userRepository.countByIsActiveTrue();
		long inactiveUsers = userRepository.countByIsActiveFalse();
		long adminUsers = userRepository.countByRole(User.Role.ADMIN);
		long regularUsers = userRepository.countByRole(User.Role.USER);

		LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
		long newUsersThisMonth = userRepository.countByCreatedAtAfter(monthAgo);

		return UserStatsDto.builder()
				.totalUsers(totalUsers)
				.activeUsers(activeUsers)
				.inactiveUsers(inactiveUsers)
				.adminUsers(adminUsers)
				.regularUsers(regularUsers)
				.newUsersThisMonth(newUsersThisMonth)
				.build();
	}

	/**
	 * Obtiene usuarios con tokens expirados
	 */
	@Transactional(readOnly = true)
	public List<UserDto> getUsersWithExpiredTokens() {
		LocalDateTime cutoffDate = LocalDateTime.now().minusHours(TOKEN_EXPIRY_HOURS);
		List<User> users = userRepository.findByTokensGreaterThanAndLastTokenUpdateBefore(0, cutoffDate);

		return users.stream().map(this::convertToDto).toList();
	}

	/**
	 * Limpia tokens expirados (tarea programada)
	 */
	@Scheduled(fixedRate = 3600000) // Cada hora
	@Transactional
	public void cleanupExpiredTokens() {
		log.info("Ejecutando limpieza de tokens expirados");

		LocalDateTime cutoffDate = LocalDateTime.now().minusHours(TOKEN_EXPIRY_HOURS);
		List<User> usersWithExpiredTokens = userRepository.findByTokensGreaterThanAndLastTokenUpdateBefore(0,
				cutoffDate);

		int cleanedUsers = 0;
		for (User user : usersWithExpiredTokens) {
			user.setTokens(0);
			userRepository.save(user);
			cleanedUsers++;
		}

		if (cleanedUsers > 0) {
			log.info("Tokens expirados limpiados para {} usuarios", cleanedUsers);
		}
	}

	/**
	 * Elimina usuario (soft delete)
	 */
	@CacheEvict(value = "users", key = "#userId")
	public void deleteUser(String userId, String adminUserId) {
		log.info("Eliminando usuario: {} por admin: {}", userId, adminUserId);

		// Validar admin
		User admin = userRepository.findById(adminUserId)
				.orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

		if (!admin.getRole().equals(User.Role.ADMIN)) {
			throw new RuntimeException("Solo los administradores pueden eliminar usuarios");
		}

		// No permitir eliminar al propio admin
		if (userId.equals(adminUserId)) {
			throw new RuntimeException("No puedes eliminar tu propia cuenta");
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		// Soft delete - desactivar usuario
		user.setIsActive(false);
		user.setTokens(0);
		userRepository.save(user);

		log.info("Usuario {} eliminado exitosamente", userId);
	}

	// ================= MÉTODOS PRIVADOS =================

	private void validatePassword(String password) {
		if (password == null || password.length() < 6) {
			throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
		}

		if (!password.matches(".*[A-Za-z].*")) {
			throw new IllegalArgumentException("La contraseña debe contener al menos una letra");
		}

		if (!password.matches(".*[0-9].*")) {
			throw new IllegalArgumentException("La contraseña debe contener al menos un número");
		}
	}

	private UserDto convertToDto(User user) {
		return UserDto.builder()
				.id(user.getId())
				.email(user.getEmail())
				.name(user.getName())
				.role(user.getRole())
				.tokens(user.getTokens())
				.createdAt(user.getCreatedAt())
				.isActive(user.getIsActive())
				.lastTokenUpdate(user.getLastTokenUpdate())
				.build();
	}

	// ================= DTO PARA ESTADÍSTICAS =================

	public static class UserStatsDto {
		private final long totalUsers;
		private final long activeUsers;
		private final long inactiveUsers;
		private final long adminUsers;
		private final long regularUsers;
		private final long newUsersThisMonth;

		public UserStatsDto(long totalUsers, long activeUsers, long inactiveUsers,
				long adminUsers, long regularUsers, long newUsersThisMonth) {
			this.totalUsers = totalUsers;
			this.activeUsers = activeUsers;
			this.inactiveUsers = inactiveUsers;
			this.adminUsers = adminUsers;
			this.regularUsers = regularUsers;
			this.newUsersThisMonth = newUsersThisMonth;
		}

		public static UserStatsDto.UserStatsDtoBuilder builder() {
			return new UserStatsDto.UserStatsDtoBuilder();
		}

		// Getters
		public long getTotalUsers() {
			return totalUsers;
		}

		public long getActiveUsers() {
			return activeUsers;
		}

		public long getInactiveUsers() {
			return inactiveUsers;
		}

		public long getAdminUsers() {
			return adminUsers;
		}

		public long getRegularUsers() {
			return regularUsers;
		}

		public long getNewUsersThisMonth() {
			return newUsersThisMonth;
		}

		public static class UserStatsDtoBuilder {
			private long totalUsers;
			private long activeUsers;
			private long inactiveUsers;
			private long adminUsers;
			private long regularUsers;
			private long newUsersThisMonth;

			public UserStatsDtoBuilder totalUsers(long totalUsers) {
				this.totalUsers = totalUsers;
				return this;
			}

			public UserStatsDtoBuilder activeUsers(long activeUsers) {
				this.activeUsers = activeUsers;
				return this;
			}

			public UserStatsDtoBuilder inactiveUsers(long inactiveUsers) {
				this.inactiveUsers = inactiveUsers;
				return this;
			}

			public UserStatsDtoBuilder adminUsers(long adminUsers) {
				this.adminUsers = adminUsers;
				return this;
			}

			public UserStatsDtoBuilder regularUsers(long regularUsers) {
				this.regularUsers = regularUsers;
				return this;
			}

			public UserStatsDtoBuilder newUsersThisMonth(long newUsersThisMonth) {
				this.newUsersThisMonth = newUsersThisMonth;
				return this;
			}

			public UserStatsDto build() {
				return new UserStatsDto(totalUsers, activeUsers, inactiveUsers,
						adminUsers, regularUsers, newUsersThisMonth);
			}
		}
	}
}