package com.arojas.jce_consulta_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.arojas.jce_consulta_api.entity.PaymentOrder;
import com.fasterxml.jackson.annotation.JsonFormat;

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
public class PaymentOrderDto {

	private String id;
	private String userId;
	private Integer tokens;
	private BigDecimal amount;
	private PaymentOrder.PaymentStatus status;
	private String buyMeCoffeeUrl;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime completedAt;

	private String errorMessage;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

	public static PaymentOrderDto fromEntity(PaymentOrder order) {
		return PaymentOrderDto.builder()
				.id(order.getId())
				.userId(order.getUser().getId())
				.tokens(order.getTokens())
				.amount(order.getAmount())
				.status(order.getStatus())
				.buyMeCoffeeUrl(order.getBuyMeCoffeeUrl())
				.createdAt(order.getCreatedAt())
				.completedAt(order.getCompletedAt())
				.errorMessage(order.getErrorMessage())
				.build();
	}
}
