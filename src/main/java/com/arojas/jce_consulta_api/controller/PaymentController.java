package com.arojas.jce_consulta_api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arojas.jce_consulta_api.dto.PaymentOrderDto;
import com.arojas.jce_consulta_api.dto.response.ApiResponse;
import com.arojas.jce_consulta_api.dto.response.PaginatedResponse;
import com.arojas.jce_consulta_api.service.PaymentService;
import com.arojas.jce_consulta_api.service.PaymentService.PaymentStatsDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para gestión de pagos y compra de tokens
 * Maneja tanto endpoints de usuario como administrativos
 *
 * @author arojas
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Payments", description = "Gestión de pagos y compra de tokens")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

	private final PaymentService paymentService;

	// ================= USER ENDPOINTS =================

	@Operation(summary = "Crear orden de pago", description = "Crea una nueva orden de pago para comprar tokens")
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Orden de pago creada exitosamente", content = @Content(schema = @Schema(implementation = PaymentOrderDto.class))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de pago inválidos"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Usuario inactivo")
	})
	@PostMapping("/create-order")
	public ResponseEntity<ApiResponse<PaymentOrderDto>> createPaymentOrder(
			@Parameter(description = "Cantidad de tokens a comprar") @RequestParam @Min(1) @Max(100) int tokens,
			Authentication authentication) {

		log.info("Creating payment order for {} tokens - user: {}", tokens, authentication.getName());

		PaymentOrderDto paymentOrder = paymentService.createPaymentOrder(authentication.getName(), tokens);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(paymentOrder, "Orden de pago creada exitosamente"));
	}

	@Operation(summary = "Obtener historial de pagos", description = "Obtiene el historial paginado de pagos del usuario")
	@GetMapping("/history")
	public ResponseEntity<ApiResponse<PaginatedResponse<PaymentOrderDto>>> getPaymentHistory(
			@Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") @Min(0) int page,
			@Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
			Authentication authentication) {

		log.info("Getting payment history for user: {}", authentication.getName());

		Page<PaymentOrderDto> historyPage = paymentService.getUserPaymentHistory(
				authentication.getName(), page, size);

		PaginatedResponse<PaymentOrderDto> paginatedResponse = PaginatedResponse.from(historyPage);

		return ResponseEntity.ok(
				ApiResponse.success(paginatedResponse, "Historial de pagos obtenido exitosamente"));
	}

	@Operation(summary = "Obtener orden de pago por ID", description = "Obtiene los detalles de una orden de pago específica")
	@GetMapping("/{paymentId}")
	public ResponseEntity<ApiResponse<PaymentOrderDto>> getPaymentOrder(
			@Parameter(description = "ID de la orden de pago") @PathVariable @NotBlank String paymentId,
			Authentication authentication) {

		log.info("Getting payment order {} for user: {}", paymentId, authentication.getName());

		PaymentOrderDto paymentOrder = paymentService.getPaymentOrderById(
				paymentId, authentication.getName());

		return ResponseEntity.ok(
				ApiResponse.success(paymentOrder, "Orden de pago obtenida exitosamente"));
	}

	@Operation(summary = "Obtener estadísticas de pagos", description = "Obtiene estadísticas de los pagos del usuario")
	@GetMapping("/stats")
	public ResponseEntity<ApiResponse<PaymentStatsDto>> getPaymentStats(Authentication authentication) {

		log.info("Getting payment stats for user: {}", authentication.getName());

		PaymentStatsDto stats = paymentService.getUserPaymentStats(authentication.getName());

		return ResponseEntity.ok(
				ApiResponse.success(stats, "Estadísticas de pagos obtenidas exitosamente"));
	}

	// ================= ADMIN ENDPOINTS =================

	@Operation(summary = "Obtener pagos pendientes (Admin)", description = "Lista todos los pagos pendientes de confirmación")
	@GetMapping("/pending")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<List<PaymentOrderDto>>> getPendingPayments() {

		log.info("Getting pending payments - Admin request");

		List<PaymentOrderDto> pendingPayments = paymentService.getPendingPayments();

		return ResponseEntity.ok(
				ApiResponse.success(pendingPayments, "Pagos pendientes obtenidos exitosamente"));
	}

	@Operation(summary = "Confirmar pago manualmente (Admin)", description = "Confirma un pago pendiente manualmente")
	@PostMapping("/{paymentId}/confirm")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<PaymentOrderDto>> confirmPayment(
			@Parameter(description = "ID de la orden de pago") @PathVariable @NotBlank String paymentId,
			Authentication authentication) {

		log.info("Admin {} confirming payment: {}", authentication.getName(), paymentId);

		PaymentOrderDto confirmedPayment = paymentService.confirmPayment(paymentId, authentication.getName());

		return ResponseEntity.ok(
				ApiResponse.success(confirmedPayment, "Pago confirmado exitosamente"));
	}

	@Operation(summary = "Marcar pago como fallido (Admin)", description = "Marca un pago como fallido con una razón específica")
	@PostMapping("/{paymentId}/fail")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<PaymentOrderDto>> failPayment(
			@Parameter(description = "ID de la orden de pago") @PathVariable @NotBlank String paymentId,
			@Parameter(description = "Razón del fallo") @RequestParam @NotBlank @Size(max = 500) String reason) {

		log.info("Failing payment {} with reason: {}", paymentId, reason);

		PaymentOrderDto failedPayment = paymentService.failPayment(paymentId, reason);

		return ResponseEntity.ok(
				ApiResponse.success(failedPayment, "Pago marcado como fallido"));
	}

	@Operation(summary = "Obtener pagos expirados (Admin)", description = "Lista pagos pendientes que han expirado")
	@GetMapping("/expired")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<List<PaymentOrderDto>>> getExpiredPayments(
			@Parameter(description = "Horas de antigüedad para considerar expirado") @RequestParam(defaultValue = "24") @Min(1) @Max(168) int hoursOld) {

		log.info("Getting expired payments (older than {} hours)", hoursOld);

		List<PaymentOrderDto> expiredPayments = paymentService.getExpiredPendingPayments(hoursOld);

		return ResponseEntity.ok(
				ApiResponse.success(expiredPayments, "Pagos expirados obtenidos exitosamente"));
	}

	@Operation(summary = "Limpiar pagos expirados (Admin)", description = "Marca como fallidos los pagos pendientes expirados")
	@PostMapping("/cleanup-expired")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Integer>> cleanupExpiredPayments(
			@Parameter(description = "Horas de antigüedad para considerar expirado") @RequestParam(defaultValue = "24") @Min(1) @Max(168) int hoursOld) {

		log.info("Starting cleanup of expired payments (older than {} hours)", hoursOld);

		int cleanedCount = paymentService.cleanupExpiredPendingPayments(hoursOld);

		log.info("Cleanup completed: {} payments marked as failed", cleanedCount);

		return ResponseEntity.ok(
				ApiResponse.success(cleanedCount, "Limpieza de pagos expirados completada"));
	}

	// ================= WEBHOOK ENDPOINTS =================

	@Operation(summary = "Webhook de Buy Me a Coffee", description = "Endpoint para recibir notificaciones de pago de Buy Me a Coffee")
	@PostMapping("/webhook/buymeacoffee")
	public ResponseEntity<ApiResponse<String>> buyMeACoffeeWebhook(
			@RequestBody String payload,
			@RequestHeader(value = "X-Signature", required = false) String signature) {

		log.info("Received Buy Me a Coffee webhook");
		log.debug("Payload: {}", payload);
		log.debug("Signature: {}", signature);

		String result = paymentService.processBuyMeACoffeeWebhook(payload, signature);

		return ResponseEntity.ok(
				ApiResponse.success(result, "Webhook procesado correctamente"));
	}
}