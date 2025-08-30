package com.arojas.jce_consulta_api.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arojas.jce_consulta_api.dto.CedulaQueryDto;
import com.arojas.jce_consulta_api.dto.CedulaResultDto;
import com.arojas.jce_consulta_api.dto.UserDto;
import com.arojas.jce_consulta_api.entity.CedulaQuery;
import com.arojas.jce_consulta_api.entity.CedulaQuery.QueryStatus;
import com.arojas.jce_consulta_api.entity.CedulaResult;
import com.arojas.jce_consulta_api.entity.User;
import com.arojas.jce_consulta_api.exception.query.CedulaQueryExceptions;
import com.arojas.jce_consulta_api.repository.CedulaQueryRepository;
import com.arojas.jce_consulta_api.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para gestión de consultas de cédulas dominicanas
 * Implementa validaciones, transaccionalidad y manejo robusto de errores
 *
 * @author arojas
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CedulaQueryService {

	// Repositories
	private final CedulaQueryRepository cedulaQueryRepository;
	private final UserRepository userRepository;

	// Services
	private final JceClient jceClient;
	private final UserService userService;
	private final AppSettingsService appSettingsService;

	// Constants
	private static final BigDecimal QUERY_COST = BigDecimal.ONE; // 1 token per query

	// ================= QUERY OPERATIONS =================

	/**
	 * Realiza una consulta asíncrona de cédula
	 */
	@Async
	public CompletableFuture<CedulaQueryDto> performCedulaQueryAsync(String cedula, String userEmail) {
		log.info("Starting async cedula query: {} for user: {}", cedula, userEmail);

		return CompletableFuture.supplyAsync(() -> {
			try {
				return performCedulaQuery(cedula, userEmail);
			} catch (Exception e) {
				log.error("Error in async cedula query: {}", e.getMessage(), e);
				throw CedulaQueryExceptions.processingError(cedula, e.getMessage(), e);
			}
		});
	}

	/**
	 * Realiza una consulta síncrona de cédula
	 */
	public CedulaQueryDto performCedulaQuery(String cedula, String userEmail) {
		log.info("Performing cedula query: {} for user: {}", cedula, userEmail);

		validateCedulaFormat(cedula);
		User user = getUserByEmailOrThrow(userEmail);
		validateUserCanQuery(user);

		CedulaQuery query = createPendingQuery(cedula, user);

		try {
			// Consume token before query
			consumeUserToken(user);

			// Perform JCE query
			CedulaResultDto result = queryJceService(cedula);

			// Update query with result
			updateQueryWithSuccess(query, result);

			log.info("Cedula query completed successfully: {}", cedula);
			return convertToDto(query);

		} catch (Exception e) {
			log.error("Error performing cedula query {}: {}", cedula, e.getMessage());

			// Refund token on error
			refundUserToken(user);
			updateQueryWithError(query, e.getMessage());

			throw CedulaQueryExceptions.processingError(cedula, e.getMessage(), e);
		}
	}

	/**
	 * Verifica si el usuario puede realizar consultas
	 */
	@Transactional(readOnly = true)
	public boolean canUserQuery(String userEmail) {
		log.debug("Checking if user can query: {}", userEmail);

		User user = getUserByEmailOrThrow(userEmail);
		return user.getTokens() > 0 && user.getIsActive();
	}

	// ================= HISTORY OPERATIONS =================

	/**
	 * Obtiene el historial paginado de consultas del usuario
	 */
	@Transactional(readOnly = true)
	public Page<CedulaQueryDto> getUserQueryHistory(String userEmail, int page, int size,
			String sortBy, String sortDir) {
		log.info("Getting query history for user: {} - page: {}", userEmail, page);

		User user = getUserByEmailOrThrow(userEmail);

		Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
		Pageable pageable = PageRequest.of(page, size, sort);

		Page<CedulaQuery> queries = cedulaQueryRepository.findByUserIdOrderByQueryDateDesc(
				user.getId(), pageable);

		return queries.map(this::convertToDto);
	}

	/**
	 * Obtiene una consulta específica por ID
	 */
	@Transactional(readOnly = true)
	public CedulaQueryDto getQueryById(String queryId, String userEmail) {
		log.info("Getting query: {} for user: {}", queryId, userEmail);

		User user = getUserByEmailOrThrow(userEmail);

		CedulaQuery query = cedulaQueryRepository.findByIdAndUserId(queryId, user.getId())
				.orElseThrow(() -> CedulaQueryExceptions.queryNotFound(queryId));

		return convertToDto(query);
	}

	/**
	 * Obtiene las consultas más recientes del usuario
	 */
	@Transactional(readOnly = true)
	public List<CedulaQueryDto> getRecentQueries(String userEmail, int limit) {
		log.info("Getting recent queries for user: {} - limit: {}", userEmail, limit);

		User user = getUserByEmailOrThrow(userEmail);

		Pageable pageable = PageRequest.of(0, limit,
				Sort.by(Sort.Direction.DESC, "queryDate"));

		Page<CedulaQuery> page = cedulaQueryRepository.findByUserId(user.getId(), pageable);

		return page.getContent().stream().map(this::convertToDto).toList();
	}

	// ================= SEARCH OPERATIONS =================

	/**
	 * Busca consultas por número de cédula
	 */
	@Transactional(readOnly = true)
	public List<CedulaQueryDto> searchQueriesByCedula(String userEmail, String cedula) {
		log.info("Searching queries by cedula for user: {}", userEmail);

		User user = getUserByEmailOrThrow(userEmail);

		List<CedulaQuery> queries = cedulaQueryRepository
				.findByUserIdAndCedulaContainingOrderByQueryDateDesc(user.getId(), cedula);

		return queries.stream().map(this::convertToDto).toList();
	}

	// ================= STATS OPERATIONS =================

	/**
	 * Obtiene estadísticas de consultas del usuario
	 */
	@Transactional(readOnly = true)
	public CedulaQueryStatsDto getUserQueryStats(String userEmail) {
		log.info("Getting query stats for user: {}", userEmail);

		User user = getUserByEmailOrThrow(userEmail);

		return buildQueryStats(user.getId());
	}

	// ================= PRIVATE HELPER METHODS =================

	private User getUserByEmailOrThrow(String email) {
		UserDto userDto = userService.getUserByEmail(email)
				.orElseThrow(() -> CedulaQueryExceptions.userNotFound(email));

		return userRepository.findById(userDto.getId())
				.orElseThrow(() -> CedulaQueryExceptions.userNotFound(email));
	}

	private void validateCedulaFormat(String cedula) {
		if (cedula == null || cedula.trim().isEmpty()) {
			throw CedulaQueryExceptions.invalidFormat(cedula, "La cédula no puede estar vacía");
		}

		String cleanCedula = cedula.replaceAll("\\D", "");

		if (cleanCedula.length() != 11) {
			throw CedulaQueryExceptions.invalidFormat(cedula, "La cédula debe tener 11 dígitos");
		}

		if (!isValidDominicanCedula(cleanCedula)) {
			throw CedulaQueryExceptions.invalidFormat(cedula, "Formato de cédula dominicana inválido");
		}
	}

	private boolean isValidDominicanCedula(String cedula) {
		// Validate that it's not all zeros or all the same digit
		return !cedula.matches("0{11}") && !cedula.matches("(\\d)\\1{10}");
	}

	private void validateUserCanQuery(User user) {
		if (!user.getIsActive()) {
			throw CedulaQueryExceptions.userInactive(user.getEmail());
		}

		if (user.getTokens() <= 0) {
			throw CedulaQueryExceptions.insufficientTokens(user.getId(), user.getTokens(), 1);
		}
	}

	private CedulaQuery createPendingQuery(String cedula, User user) {
		CedulaQuery query = CedulaQuery.builder()
				.cedula(cedula)
				.user(user)
				.queryDate(LocalDateTime.now())
				.cost(QUERY_COST)
				.status(QueryStatus.PENDING)
				.build();

		return cedulaQueryRepository.save(query);
	}

	private void consumeUserToken(User user) {
		try {
			userService.consumeToken(user.getId());
		} catch (Exception e) {
			throw CedulaQueryExceptions.insufficientTokens(user.getId());
		}
	}

	private void refundUserToken(User user) {
		try {
			userService.addTokens(user.getId(), 1);
		} catch (Exception e) {
			log.error("Error refunding token for user {}: {}", user.getId(), e.getMessage());
		}
	}

	private CedulaResultDto queryJceService(String cedula) {
		try {
			return jceClient.queryCedula(cedula);
		} catch (Exception e) {
			log.error("JCE service error for cedula {}: {}", cedula, e.getMessage());
			throw CedulaQueryExceptions.jceUnavailable(e.getMessage(), e);
		}
	}

	private void updateQueryWithSuccess(CedulaQuery query, CedulaResultDto resultDto) {
		CedulaResult result = buildCedulaResult(resultDto);

		query.setResult(result);
		query.setStatus(QueryStatus.COMPLETED);
		query.setCompletedAt(LocalDateTime.now());

		cedulaQueryRepository.save(query);
	}

	private void updateQueryWithError(CedulaQuery query, String errorMessage) {
		query.setStatus(QueryStatus.FAILED);
		query.setErrorMessage(errorMessage);
		query.setCompletedAt(LocalDateTime.now());

		cedulaQueryRepository.save(query);
	}

	private CedulaResult buildCedulaResult(CedulaResultDto resultDto) {
		LocalDate fechaNac = null;
		if (resultDto.getFechaNacimiento() != null && !resultDto.getFechaNacimiento().isEmpty()) {
			fechaNac = LocalDate.parse(resultDto.getFechaNacimiento(), DateTimeFormatter.ISO_DATE);
			// Ajusta el formato si tu string viene en otro pattern
			// Ejemplo: "dd/MM/yyyy"
			// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			// fechaNac = LocalDate.parse(resultDto.getFechaNacimiento(), formatter);
		}

		return CedulaResult.builder()
				.nombres(resultDto.getNombres())
				.apellidos(resultDto.getApellidos())
				.fechaNacimiento(fechaNac)
				.lugarNacimiento(resultDto.getLugarNacimiento())
				.estadoCivil(resultDto.getEstadoCivil())
				.ocupacion(resultDto.getOcupacion())
				.nacionalidad(resultDto.getNacionalidad())
				.sexo(resultDto.getSexo())
				.foto(resultDto.getFoto())
				.build();
	}

	private CedulaQueryStatsDto buildQueryStats(String userId) {
		long totalQueries = cedulaQueryRepository.countByUserId(userId);
		long completedQueries = cedulaQueryRepository.countByUserIdAndStatus(userId, QueryStatus.COMPLETED);
		long failedQueries = cedulaQueryRepository.countByUserIdAndStatus(userId, QueryStatus.FAILED);
		long pendingQueries = cedulaQueryRepository.countByUserIdAndStatus(userId, QueryStatus.PENDING);

		LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
		long todayQueries = cedulaQueryRepository.countByUserIdAndQueryDateAfter(userId, today);

		return CedulaQueryStatsDto.builder()
				.totalQueries(totalQueries)
				.completedQueries(completedQueries)
				.failedQueries(failedQueries)
				.pendingQueries(pendingQueries)
				.todayQueries(todayQueries)
				.build();
	}

	private CedulaQueryDto convertToDto(CedulaQuery query) {
		CedulaResultDto resultDto = null;

		if (query.getResult() != null) {
			CedulaResult result = query.getResult();
			resultDto = CedulaResultDto.builder()
					.nombres(result.getNombres())
					.apellido1(result.getApellidos())
					.apellido2(result.getApellidos())
					.fechaNacimiento(
							result.getFechaNacimiento() != null
									? result.getFechaNacimiento().format(DateTimeFormatter.ISO_DATE)
									: null)
					.lugarNacimiento(result.getLugarNacimiento())
					.estadoCivil(result.getEstadoCivil())
					.ocupacion(result.getOcupacion())
					.nacionalidad(result.getNacionalidad())
					.sexo(result.getSexo())
					.fotoUrl(result.getFoto())
					.build();
		}

		return CedulaQueryDto.builder()
				.id(query.getId())
				.cedula(query.getCedula())
				.queryDate(query.getQueryDate())
				.userId(query.getUser().getId())
				.result(resultDto)
				.cost(query.getCost())
				.status(query.getStatus())
				.errorMessage(query.getErrorMessage())
				.completedAt(query.getCompletedAt())
				.build();
	}

	// ================= NESTED DTO CLASS =================

	/**
	 * DTO para estadísticas de consultas del usuario
	 */
	public static class CedulaQueryStatsDto {
		private final long totalQueries;
		private final long completedQueries;
		private final long failedQueries;
		private final long pendingQueries;
		private final long todayQueries;

		public CedulaQueryStatsDto(long totalQueries, long completedQueries, long failedQueries,
				long pendingQueries, long todayQueries) {
			this.totalQueries = totalQueries;
			this.completedQueries = completedQueries;
			this.failedQueries = failedQueries;
			this.pendingQueries = pendingQueries;
			this.todayQueries = todayQueries;
		}

		public static CedulaQueryStatsDtoBuilder builder() {
			return new CedulaQueryStatsDtoBuilder();
		}

		// Getters
		public long getTotalQueries() {
			return totalQueries;
		}

		public long getCompletedQueries() {
			return completedQueries;
		}

		public long getFailedQueries() {
			return failedQueries;
		}

		public long getPendingQueries() {
			return pendingQueries;
		}

		public long getTodayQueries() {
			return todayQueries;
		}

		public static class CedulaQueryStatsDtoBuilder {
			private long totalQueries;
			private long completedQueries;
			private long failedQueries;
			private long pendingQueries;
			private long todayQueries;

			public CedulaQueryStatsDtoBuilder totalQueries(long totalQueries) {
				this.totalQueries = totalQueries;
				return this;
			}

			public CedulaQueryStatsDtoBuilder completedQueries(long completedQueries) {
				this.completedQueries = completedQueries;
				return this;
			}

			public CedulaQueryStatsDtoBuilder failedQueries(long failedQueries) {
				this.failedQueries = failedQueries;
				return this;
			}

			public CedulaQueryStatsDtoBuilder pendingQueries(long pendingQueries) {
				this.pendingQueries = pendingQueries;
				return this;
			}

			public CedulaQueryStatsDtoBuilder todayQueries(long todayQueries) {
				this.todayQueries = todayQueries;
				return this;
			}

			public CedulaQueryStatsDto build() {
				return new CedulaQueryStatsDto(totalQueries, completedQueries, failedQueries,
						pendingQueries, todayQueries);
			}
		}
	}
}