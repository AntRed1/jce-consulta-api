/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.arojas.jce_consulta_api.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arojas.jce_consulta_api.entity.RefreshToken;
import com.arojas.jce_consulta_api.entity.User;

/**
 *
 * @author arojas
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

	Optional<RefreshToken> findByToken(String token);

	boolean existsByTokenAndIsRevokedFalse(String token);

	/** Eliminar todos los tokens de un usuario */
	@Modifying
	@Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
	void deleteAllByUserId(@Param("userId") String userId);

	/** Eliminar tokens expirados o revocados */
	@Modifying
	@Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now OR rt.isRevoked = true")
	void deleteExpiredAndRevokedTokens(@Param("now") LocalDateTime now);

	/** Revocar todos los tokens de un usuario */
	@Modifying
	@Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user.id = :userId")
	void revokeAllUserTokens(@Param("userId") String userId);

	void deleteAllByUser(User user);
}
