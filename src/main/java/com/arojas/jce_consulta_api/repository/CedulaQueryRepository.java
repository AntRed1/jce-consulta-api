/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.arojas.jce_consulta_api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arojas.jce_consulta_api.entity.CedulaQuery;

/**
 *
 * @author arojas
 */
@Repository
public interface CedulaQueryRepository extends JpaRepository<CedulaQuery, String> {

	Page<CedulaQuery> findByUserIdOrderByQueryDateDesc(String userId, Pageable pageable);

	Optional<CedulaQuery> findByCedulaAndUserId(String cedula, String userId);

	/** Consultas completadas de un usuario después de una fecha */
	@Query("SELECT cq FROM CedulaQuery cq WHERE cq.user.id = :userId AND cq.status = 'COMPLETED' AND cq.queryDate >= :date")
	List<CedulaQuery> findCompletedQueriesByUserAfterDate(@Param("userId") String userId,
			@Param("date") LocalDateTime date);

	/** Número de consultas completadas por cédula y usuario */
	@Query("SELECT COUNT(cq) FROM CedulaQuery cq WHERE cq.cedula = :cedula AND cq.user.id = :userId AND cq.status = 'COMPLETED'")
	long countCompletedQueriesByCedulaAndUser(@Param("cedula") String cedula, @Param("userId") String userId);

	/** Consultas por estado */
	@Query("SELECT cq FROM CedulaQuery cq WHERE cq.status = :status ORDER BY cq.queryDate DESC")
	Page<CedulaQuery> findByStatus(@Param("status") CedulaQuery.QueryStatus status, Pageable pageable);

	/** Contar consultas entre dos fechas */
	@Query("SELECT COUNT(cq) FROM CedulaQuery cq WHERE cq.queryDate >= :startDate AND cq.queryDate < :endDate")
	long countQueriesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	List<CedulaQuery> findByUserIdOrderByQueryDateDesc(String userId);

	Optional<CedulaQuery> findByIdAndUserId(String id, String userId);

	long countByUserId(String userId);

	long countByUserIdAndStatus(String userId, CedulaQuery.QueryStatus status);

	long countByUserIdAndQueryDateAfter(String userId, LocalDateTime dateTime);

	Page<CedulaQuery> findByUserId(String userId, Pageable pageable);

	List<CedulaQuery> findByUserIdAndCedulaContainingOrderByQueryDateDesc(String userId, String cedula);

}
