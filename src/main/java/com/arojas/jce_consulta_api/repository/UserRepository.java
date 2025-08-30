/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arojas.jce_consulta_api.entity.User;

/**
 *
 * @author arojas
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

	/** Buscar usuario por email */
	Optional<User> findByEmail(String email);

	/** Verificar si existe usuario por email */
	boolean existsByEmail(String email);

	/** Listar usuarios activos */
	Page<User> findByIsActiveTrue(Pageable pageable);

	/** Listar usuarios activos con tokens */
	@Query("SELECT u FROM User u WHERE u.isActive = true AND u.tokens > 0")
	Page<User> findActiveUsersWithTokens(Pageable pageable);

	/** Agregar tokens al usuario */
	@Modifying
	@Query("UPDATE User u SET u.tokens = u.tokens + :amount WHERE u.id = :userId")
	int addTokensToUser(@Param("userId") String userId, @Param("amount") Integer amount);

	/** Descontar tokens del usuario */
	@Modifying
	@Query("UPDATE User u SET u.tokens = u.tokens - :amount WHERE u.id = :userId AND u.tokens >= :amount")
	int deductTokensFromUser(@Param("userId") String userId, @Param("amount") Integer amount);

	/** Contar usuarios registrados después de una fecha */
	@Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
	long countUsersRegisteredAfter(@Param("date") LocalDateTime date);

	// Buscar por nombre o email (paginación)
	Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);

	// Contar usuarios activos/inactivos
	long countByIsActiveTrue();

	long countByIsActiveFalse();

	// Contar por rol
	long countByRole(User.Role role);

	// Contar usuarios creados después de una fecha
	long countByCreatedAtAfter(LocalDateTime date);

	// Buscar usuarios con tokens > 0 y con última actualización antes de una fecha
	List<User> findByTokensGreaterThanAndLastTokenUpdateBefore(int tokens, LocalDateTime date);

}
