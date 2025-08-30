/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.arojas.jce_consulta_api.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arojas.jce_consulta_api.entity.PaymentOrder;
import com.arojas.jce_consulta_api.entity.PaymentOrder.PaymentStatus;

/**
 *
 * @author arojas
 */

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, String> {

	Page<PaymentOrder> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

	List<PaymentOrder> findByUserIdOrderByCreatedAtDesc(String userId);

	Optional<PaymentOrder> findByExternalReference(String externalReference);

	Optional<PaymentOrder> findByIdAndUserId(String id, String userId);

	long countByUserId(String userId);

	long countByUserIdAndStatus(String userId, PaymentStatus status);

	@Query("SELECT SUM(po.amount) FROM PaymentOrder po WHERE po.user.id = :userId AND po.status = :status")
	BigDecimal sumAmountByUserIdAndStatus(@Param("userId") String userId, @Param("status") PaymentStatus status);

	@Query("SELECT SUM(po.tokens) FROM PaymentOrder po WHERE po.user.id = :userId AND po.status = :status")
	Integer sumTokensByUserIdAndStatus(@Param("userId") String userId, @Param("status") PaymentStatus status);

	List<PaymentOrder> findByStatusOrderByCreatedAtAsc(PaymentStatus status);

	/** Órdenes vencidas en estado pendiente */
	@Query("SELECT po FROM PaymentOrder po WHERE po.status = 'PENDING' AND po.createdAt < :expiredBefore")
	List<PaymentOrder> findExpiredPendingOrders(@Param("expiredBefore") LocalDateTime expiredBefore);

	/** Órdenes por estado y fecha */
	List<PaymentOrder> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime createdBefore);

	/** Total de ingresos en rango de fechas */
	@Query("SELECT SUM(po.amount) FROM PaymentOrder po WHERE po.status = 'COMPLETED' AND po.paymentDate >= :startDate AND po.paymentDate < :endDate")
	Double getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	/** Cantidad de pagos completados por usuario */
	@Query("SELECT COUNT(po) FROM PaymentOrder po WHERE po.user.id = :userId AND po.status = 'COMPLETED'")
	long countCompletedPaymentsByUser(@Param("userId") String userId);
}