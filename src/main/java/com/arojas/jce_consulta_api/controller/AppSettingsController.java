/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arojas.jce_consulta_api.dto.response.ApiResponse;
import com.arojas.jce_consulta_api.dto.response.PaginatedResponse;
import com.arojas.jce_consulta_api.entity.AppSettings;
import com.arojas.jce_consulta_api.entity.Banner;
import com.arojas.jce_consulta_api.entity.Feature;
import com.arojas.jce_consulta_api.entity.Testimonial;
import com.arojas.jce_consulta_api.service.AppSettingsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author arojas
 */

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "App Settings", description = "Configuraciones de la aplicación")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AppSettingsController {

	private final AppSettingsService appSettingsService;

	// ================= ENDPOINTS PÚBLICOS =================

	@Operation(summary = "Obtener configuraciones públicas", description = "Obtiene las configuraciones básicas de la aplicación visibles para todos")
	@GetMapping("/public")
	public ResponseEntity<ApiResponse<AppSettings>> getPublicSettings() {

		log.info("Obteniendo configuraciones públicas de la aplicación");

		try {
			AppSettings settings = appSettingsService.getAppSettings();

			ApiResponse<AppSettings> response = ApiResponse.<AppSettings>builder()
					.success(true)
					.data(settings)
					.message("Configuraciones públicas obtenidas exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo configuraciones públicas: {}", e.getMessage());

			ApiResponse<AppSettings> response = ApiResponse.<AppSettings>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo configuraciones")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Obtener precio actual del token", description = "Obtiene el precio unitario actual del token")
	@GetMapping("/token-price")
	public ResponseEntity<ApiResponse<BigDecimal>> getTokenPrice() {

		log.debug("Obteniendo precio actual del token");

		try {
			BigDecimal tokenPrice = appSettingsService.getTokenPrice();

			ApiResponse<BigDecimal> response = ApiResponse.<BigDecimal>builder()
					.success(true)
					.data(tokenPrice)
					.message("Precio del token obtenido exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo precio del token: {}", e.getMessage());

			ApiResponse<BigDecimal> response = ApiResponse.<BigDecimal>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo precio del token")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Obtener características activas", description = "Obtiene la lista de características activas de la aplicación")
	@GetMapping("/features")
	public ResponseEntity<ApiResponse<List<Feature>>> getActiveFeatures() {

		log.info("Obteniendo características activas");

		try {
			List<Feature> features = appSettingsService.getActiveFeatures();

			ApiResponse<List<Feature>> response = ApiResponse.<List<Feature>>builder()
					.success(true)
					.data(features)
					.message("Características activas obtenidas exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo características activas: {}", e.getMessage());

			ApiResponse<List<Feature>> response = ApiResponse.<List<Feature>>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo características")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Obtener testimonios activos", description = "Obtiene la lista de testimonios activos para mostrar en la página")
	@GetMapping("/testimonials")
	public ResponseEntity<ApiResponse<List<Testimonial>>> getActiveTestimonials() {

		log.info("Obteniendo testimonios activos");

		try {
			List<Testimonial> testimonials = appSettingsService.getActiveTestimonials();

			ApiResponse<List<Testimonial>> response = ApiResponse.<List<Testimonial>>builder()
					.success(true)
					.data(testimonials)
					.message("Testimonios activos obtenidos exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo testimonios activos: {}", e.getMessage());

			ApiResponse<List<Testimonial>> response = ApiResponse.<List<Testimonial>>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo testimonios")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Obtener banners activos", description = "Obtiene la lista de banners activos ordenados por prioridad")
	@GetMapping("/banners")
	public ResponseEntity<ApiResponse<List<Banner>>> getActiveBanners() {

		log.info("Obteniendo banners activos");

		try {
			List<Banner> banners = appSettingsService.getActiveBanners();

			ApiResponse<List<Banner>> response = ApiResponse.<List<Banner>>builder()
					.success(true)
					.data(banners)
					.message("Banners activos obtenidos exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo banners activos: {}", e.getMessage());

			ApiResponse<List<Banner>> response = ApiResponse.<List<Banner>>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo banners")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	// ================= ENDPOINTS ADMINISTRATIVOS =================

	@Operation(summary = "Actualizar configuraciones generales (Admin)", description = "Actualiza las configuraciones generales de la aplicación")
	@PutMapping
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<AppSettings>> updateAppSettings(
			@Valid @RequestBody AppSettings settings) {

		log.info("Actualizando configuraciones de la aplicación");

		try {
			AppSettings updatedSettings = appSettingsService.updateAppSettings(settings);

			ApiResponse<AppSettings> response = ApiResponse.<AppSettings>builder()
					.success(true)
					.data(updatedSettings)
					.message("Configuraciones actualizadas exitosamente")
					.build();

			log.info("Configuraciones de la aplicación actualizadas exitosamente");
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error actualizando configuraciones: {}", e.getMessage());

			ApiResponse<AppSettings> response = ApiResponse.<AppSettings>builder()
					.success(false)
					.data(null)
					.message("Error actualizando configuraciones")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	// ================= GESTIÓN DE CARACTERÍSTICAS =================

	@Operation(summary = "Listar todas las características (Admin)", description = "Obtiene todas las características con paginación")
	@GetMapping("/features/all")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<PaginatedResponse<Feature>>> getAllFeatures(
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

		log.info("Obteniendo todas las características - página: {}", page);

		try {
			Pageable pageable = PageRequest.of(page, size);
			Page<Feature> featuresPage = appSettingsService.getAllFeatures(pageable);

			PaginatedResponse<Feature> paginatedResponse = PaginatedResponse.<Feature>builder()
					.content(featuresPage.getContent())
					.totalElements(featuresPage.getTotalElements())
					.totalPages(featuresPage.getTotalPages())
					.size(featuresPage.getSize())
					.number(featuresPage.getNumber())
					.build();

			ApiResponse<PaginatedResponse<Feature>> response = ApiResponse.<PaginatedResponse<Feature>>builder()
					.success(true)
					.data(paginatedResponse)
					.message("Características obtenidas exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo características: {}", e.getMessage());

			ApiResponse<PaginatedResponse<Feature>> response = ApiResponse.<PaginatedResponse<Feature>>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo características")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Crear característica (Admin)", description = "Crea una nueva característica de la aplicación")
	@PostMapping("/features")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<Feature>> createFeature(@Valid @RequestBody Feature feature) {

		log.info("Creando nueva característica: {}", feature.getTitle());

		try {
			Feature createdFeature = appSettingsService.createFeature(feature);

			ApiResponse<Feature> response = ApiResponse.<Feature>builder()
					.success(true)
					.data(createdFeature)
					.message("Característica creada exitosamente")
					.build();

			return ResponseEntity.status(201).body(response);

		} catch (Exception e) {
			log.error("Error creando característica: {}", e.getMessage());

			ApiResponse<Feature> response = ApiResponse.<Feature>builder()
					.success(false)
					.data(null)
					.message("Error creando característica")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Actualizar característica (Admin)", description = "Actualiza una característica existente")
	@PutMapping("/features/{featureId}")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<Feature>> updateFeature(
			@PathVariable @NotBlank String featureId,
			@Valid @RequestBody Feature feature) {

		log.info("Actualizando característica: {}", featureId);

		try {
			Feature updatedFeature = appSettingsService.updateFeature(featureId, feature);

			ApiResponse<Feature> response = ApiResponse.<Feature>builder()
					.success(true)
					.data(updatedFeature)
					.message("Característica actualizada exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error actualizando característica {}: {}", featureId, e.getMessage());

			ApiResponse<Feature> response = ApiResponse.<Feature>builder()
					.success(false)
					.data(null)
					.message("Error actualizando característica")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Eliminar característica (Admin)", description = "Elimina una característica del sistema")
	@DeleteMapping("/features/{featureId}")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<String>> deleteFeature(@PathVariable @NotBlank String featureId) {

		log.info("Eliminando característica: {}", featureId);

		try {
			appSettingsService.deleteFeature(featureId);

			ApiResponse<String> response = ApiResponse.<String>builder()
					.success(true)
					.data("Característica eliminada exitosamente")
					.message("La característica ha sido eliminada del sistema")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error eliminando característica {}: {}", featureId, e.getMessage());

			ApiResponse<String> response = ApiResponse.<String>builder()
					.success(false)
					.data(null)
					.message("Error eliminando característica")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	// ================= GESTIÓN DE TESTIMONIOS =================

	@Operation(summary = "Listar todos los testimonios (Admin)", description = "Obtiene todos los testimonios con paginación")
	@GetMapping("/testimonials/all")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<PaginatedResponse<Testimonial>>> getAllTestimonials(
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

		log.info("Obteniendo todos los testimonios - página: {}", page);

		try {
			Pageable pageable = PageRequest.of(page, size);
			Page<Testimonial> testimonialsPage = appSettingsService.getAllTestimonials(pageable);

			PaginatedResponse<Testimonial> paginatedResponse = PaginatedResponse.<Testimonial>builder()
					.content(testimonialsPage.getContent())
					.totalElements(testimonialsPage.getTotalElements())
					.totalPages(testimonialsPage.getTotalPages())
					.size(testimonialsPage.getSize())
					.number(testimonialsPage.getNumber())
					.build();

			ApiResponse<PaginatedResponse<Testimonial>> response = ApiResponse.<PaginatedResponse<Testimonial>>builder()
					.success(true)
					.data(paginatedResponse)
					.message("Testimonios obtenidos exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error obteniendo testimonios: {}", e.getMessage());

			ApiResponse<PaginatedResponse<Testimonial>> response = ApiResponse.<PaginatedResponse<Testimonial>>builder()
					.success(false)
					.data(null)
					.message("Error obteniendo testimonios")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Crear testimonio (Admin)", description = "Crea un nuevo testimonio")
	@PostMapping("/testimonials")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<Testimonial>> createTestimonial(@Valid @RequestBody Testimonial testimonial) {

		log.info("Creando nuevo testimonio de: {}", testimonial.getName());

		try {
			Testimonial createdTestimonial = appSettingsService.createTestimonial(testimonial);

			ApiResponse<Testimonial> response = ApiResponse.<Testimonial>builder()
					.success(true)
					.data(createdTestimonial)
					.message("Testimonio creado exitosamente")
					.build();

			return ResponseEntity.status(201).body(response);

		} catch (Exception e) {
			log.error("Error creando testimonio: {}", e.getMessage());

			ApiResponse<Testimonial> response = ApiResponse.<Testimonial>builder()
					.success(false)
					.data(null)
					.message("Error creando testimonio")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Actualizar testimonio (Admin)", description = "Actualiza un testimonio existente")
	@PutMapping("/testimonials/{testimonialId}")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<Testimonial>> updateTestimonial(
			@PathVariable @NotBlank String testimonialId,
			@Valid @RequestBody Testimonial testimonial) {

		log.info("Actualizando testimonio: {}", testimonialId);

		try {
			Testimonial updatedTestimonial = appSettingsService.updateTestimonial(testimonialId, testimonial);

			ApiResponse<Testimonial> response = ApiResponse.<Testimonial>builder()
					.success(true)
					.data(updatedTestimonial)
					.message("Testimonio actualizado exitosamente")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error actualizando testimonio {}: {}", testimonialId, e.getMessage());

			ApiResponse<Testimonial> response = ApiResponse.<Testimonial>builder()
					.success(false)
					.data(null)
					.message("Error actualizando testimonio")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}

	@Operation(summary = "Eliminar testimonio (Admin)", description = "Elimina un testimonio del sistema")
	@DeleteMapping("/testimonials/{testimonialId}")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<String>> deleteTestimonial(@PathVariable @NotBlank String testimonialId) {

		log.info("Eliminando testimonio: {}", testimonialId);

		try {
			appSettingsService.deleteTestimonial(testimonialId);

			ApiResponse<String> response = ApiResponse.<String>builder()
					.success(true)
					.data("Testimonio eliminado exitosamente")
					.message("El testimonio ha sido eliminado del sistema")
					.build();

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error eliminando testimonio {}: {}", testimonialId, e.getMessage());

			ApiResponse<String> response = ApiResponse.<String>builder()
					.success(false)
					.data(null)
					.message("Error eliminando testimonio")
					.error(e.getMessage())
					.build();

			return ResponseEntity.badRequest().body(response);
		}
	}
}
