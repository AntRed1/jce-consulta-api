package com.arojas.jce_consulta_api.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arojas.jce_consulta_api.dto.CedulaQueryDto;
import com.arojas.jce_consulta_api.dto.request.CedulaQueryRequest;
import com.arojas.jce_consulta_api.dto.response.ApiResponse;
import com.arojas.jce_consulta_api.dto.response.PaginatedResponse;
import com.arojas.jce_consulta_api.service.CedulaQueryService;
import com.arojas.jce_consulta_api.service.CedulaQueryService.CedulaQueryStatsDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para gestión de consultas de cédulas dominicanas
 * Permite realizar consultas síncronas, asíncronas y gestionar historial
 *
 * @author arojas
 */
@RestController
@RequestMapping("/api/v1/cedula-queries")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Cedula Queries", description = "Consultas de cédulas dominicanas")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CedulaQueryController {

	private final CedulaQueryService cedulaQueryService;

	// ================= QUERY ENDPOINTS =================

	@Operation(summary = "Realizar consulta de cédula", description = "Consulta información de una cédula dominicana en la JCE")
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta realizada exitosamente", content = @Content(schema = @Schema(implementation = CedulaQueryDto.class))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Formato de cédula inválido"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "402", description = "No tiene tokens suficientes"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Servicio JCE no disponible")
	})
	@PostMapping("/query")
	public ResponseEntity<ApiResponse<CedulaQueryDto>> queryCedula(
			@Valid @RequestBody CedulaQueryRequest request,
			Authentication authentication) {

		log.info("Cedula query request for user: {}", authentication.getName());

		CedulaQueryDto result = cedulaQueryService.performCedulaQuery(
				request.getCedula(), authentication.getName());

		return ResponseEntity.ok(
				ApiResponse.success(result, "Consulta realizada exitosamente"));
	}

	@Operation(summary = "Realizar consulta asíncrona de cédula", description = "Inicia una consulta asíncrona de cédula y devuelve inmediatamente")
	@PostMapping("/query-async")
	public ResponseEntity<ApiResponse<String>> queryCedulaAsync(
			@Valid @RequestBody CedulaQueryRequest request,
			Authentication authentication) {

		log.info("Async cedula query request for user: {}", authentication.getName());

		CompletableFuture<CedulaQueryDto> futureResult = cedulaQueryService.performCedulaQueryAsync(
				request.getCedula(), authentication.getName());

		return ResponseEntity.status(HttpStatus.ACCEPTED).body(
				ApiResponse.success("Consulta iniciada",
						"La consulta se está procesando en segundo plano. Puedes verificar el estado en tu historial."));
	}

	@Operation(summary = "Verificar disponibilidad de consulta", description = "Verifica si el usuario puede realizar una consulta (tiene tokens disponibles)")
	@GetMapping("/can-query")
	public ResponseEntity<ApiResponse<Boolean>> canUserQuery(Authentication authentication) {

		log.debug("Checking query availability for user: {}", authentication.getName());

		boolean canQuery = cedulaQueryService.canUserQuery(authentication.getName());

		return ResponseEntity.ok(
				ApiResponse.success(canQuery,
						canQuery ? "Usuario puede realizar consultas" : "Usuario no puede realizar consultas"));
	}

	// ================= HISTORY & STATS ENDPOINTS =================

	@Operation(summary = "Obtener historial de consultas", description = "Obtiene el historial paginado de consultas del usuario")
	@GetMapping("/history")
	public ResponseEntity<ApiResponse<PaginatedResponse<CedulaQueryDto>>> getQueryHistory(
			@Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") @Min(0) int page,
			@Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
			@Parameter(description = "Campo para ordenar") @RequestParam(defaultValue = "queryDate") String sortBy,
			@Parameter(description = "Dirección del ordenamiento") @RequestParam(defaultValue = "desc") @Pattern(regexp = "asc|desc") String sortDir,
			Authentication authentication) {

		log.info("Getting query history for user: {}", authentication.getName());

		Page<CedulaQueryDto> historyPage = cedulaQueryService.getUserQueryHistory(
				authentication.getName(), page, size, sortBy, sortDir);

		PaginatedResponse<CedulaQueryDto> paginatedResponse = PaginatedResponse.from(historyPage);

		return ResponseEntity.ok(
				ApiResponse.success(paginatedResponse, "Historial obtenido exitosamente"));
	}

	@Operation(summary = "Obtener consulta por ID", description = "Obtiene los detalles de una consulta específica")
	@GetMapping("/{queryId}")
	public ResponseEntity<ApiResponse<CedulaQueryDto>> getQueryById(
			@Parameter(description = "ID de la consulta") @PathVariable @NotBlank String queryId,
			Authentication authentication) {

		log.info("Getting query {} for user: {}", queryId, authentication.getName());

		CedulaQueryDto query = cedulaQueryService.getQueryById(queryId, authentication.getName());

		return ResponseEntity.ok(
				ApiResponse.success(query, "Consulta obtenida exitosamente"));
	}

	@Operation(summary = "Obtener estadísticas de consultas", description = "Obtiene estadísticas de las consultas del usuario")
	@GetMapping("/stats")
	public ResponseEntity<ApiResponse<CedulaQueryStatsDto>> getQueryStats(Authentication authentication) {

		log.info("Getting query stats for user: {}", authentication.getName());

		CedulaQueryStatsDto stats = cedulaQueryService.getUserQueryStats(authentication.getName());

		return ResponseEntity.ok(
				ApiResponse.success(stats, "Estadísticas obtenidas exitosamente"));
	}

	@Operation(summary = "Obtener consultas recientes", description = "Obtiene las consultas más recientes del usuario")
	@GetMapping("/recent")
	public ResponseEntity<ApiResponse<List<CedulaQueryDto>>> getRecentQueries(
			@Parameter(description = "Límite de consultas a obtener") @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit,
			Authentication authentication) {

		log.info("Getting recent queries for user: {}", authentication.getName());

		List<CedulaQueryDto> recentQueries = cedulaQueryService.getRecentQueries(
				authentication.getName(), limit);

		return ResponseEntity.ok(
				ApiResponse.success(recentQueries, "Consultas recientes obtenidas exitosamente"));
	}

	// ================= SEARCH ENDPOINTS =================

	@Operation(summary = "Buscar consultas por cédula", description = "Busca en el historial consultas que contengan el número de cédula especificado")
	@GetMapping("/search")
	public ResponseEntity<ApiResponse<List<CedulaQueryDto>>> searchQueriesByCedula(
			@Parameter(description = "Número de cédula a buscar") @RequestParam @NotBlank(message = "La cédula es requerida") @Pattern(regexp = "\\d{1,11}", message = "La cédula debe contener solo números") String cedula,
			Authentication authentication) {

		log.info("Searching queries by cedula for user: {}", authentication.getName());

		List<CedulaQueryDto> queries = cedulaQueryService.searchQueriesByCedula(
				authentication.getName(), cedula);

		return ResponseEntity.ok(
				ApiResponse.success(queries, "Búsqueda completada exitosamente"));
	}
}