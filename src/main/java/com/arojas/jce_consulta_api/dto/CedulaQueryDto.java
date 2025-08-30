package com.arojas.jce_consulta_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.arojas.jce_consulta_api.entity.CedulaQuery;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la consulta de cédula
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CedulaQueryDto {

	private String id;
	private String cedula;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime queryDate;

	private String userId;
	private CedulaResultDto result;
	private BigDecimal cost;
	private CedulaQuery.QueryStatus status;
	private String errorMessage; // agregado
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime completedAt; // agregado

	public static CedulaQueryDto fromEntity(CedulaQuery query) {
		return CedulaQueryDto.builder()
				.id(query.getId())
				.cedula(query.getCedula())
				.queryDate(query.getQueryDate())
				.userId(query.getUser().getId())
				.result(query.getResult() != null ? CedulaResultDto.fromEntity(query.getResult()) : null)
				.cost(query.getCost())
				.status(query.getStatus())
				.errorMessage(query.getErrorMessage())
				.completedAt(query.getCompletedAt())
				.build();
	}
}
