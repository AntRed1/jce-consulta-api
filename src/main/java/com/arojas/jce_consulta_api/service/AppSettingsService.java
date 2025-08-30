/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arojas.jce_consulta_api.entity.AppSettings;
import com.arojas.jce_consulta_api.entity.Banner;
import com.arojas.jce_consulta_api.entity.EmailTemplate;
import com.arojas.jce_consulta_api.entity.Feature;
import com.arojas.jce_consulta_api.entity.Testimonial;
import com.arojas.jce_consulta_api.repository.AppSettingsRepository;
import com.arojas.jce_consulta_api.repository.BannerRepository;
import com.arojas.jce_consulta_api.repository.EmailTemplateRepository;
import com.arojas.jce_consulta_api.repository.FeatureRepository;
import com.arojas.jce_consulta_api.repository.TestimonialRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author arojas
 */

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppSettingsService {

	private final AppSettingsRepository appSettingsRepository;
	private final FeatureRepository featureRepository;
	private final TestimonialRepository testimonialRepository;
	private final BannerRepository bannerRepository;
	private final EmailTemplateRepository emailTemplateRepository;

	// ================= APP SETTINGS =================

	@Cacheable("appSettings")
	@Transactional(readOnly = true)
	public AppSettings getAppSettings() {
		log.info("Obteniendo configuraciones de la aplicación");
		return appSettingsRepository.findFirstByOrderByCreatedAtAsc()
    .orElseGet(this::createDefaultSettings);
	}

	@CacheEvict("appSettings")
	public AppSettings updateAppSettings(AppSettings settings) {
		log.info("Actualizando configuraciones de la aplicación");

		AppSettings existing = getAppSettings();
		existing.setSiteName(settings.getSiteName());
		existing.setSiteDescription(settings.getSiteDescription());
		existing.setHeroTitle(settings.getHeroTitle());
		existing.setHeroSubtitle(settings.getHeroSubtitle());
		existing.setTokenPrice(settings.getTokenPrice());

		return appSettingsRepository.save(existing);
	}

	@Cacheable("tokenPrice")
	@Transactional(readOnly = true)
	public BigDecimal getTokenPrice() {
		return getAppSettings().getTokenPrice();
	}

	@CacheEvict("tokenPrice")
	public void updateTokenPrice(BigDecimal newPrice) {
		log.info("Actualizando precio del token a: {}", newPrice);
		AppSettings settings = getAppSettings();
		settings.setTokenPrice(newPrice);
		appSettingsRepository.save(settings);
	}

	// ================= FEATURES =================

	@Cacheable("activeFeatures")
	@Transactional(readOnly = true)
	public List<Feature> getActiveFeatures() {
		return featureRepository.findByIsActiveTrueOrderByTitle();
	}

	@Transactional(readOnly = true)
	public Page<Feature> getAllFeatures(Pageable pageable) {
		return featureRepository.findAll(pageable);
	}

	@Transactional(readOnly = true)
	public Optional<Feature> getFeatureById(String id) {
		return featureRepository.findById(id);
	}

	@CacheEvict(value = "activeFeatures", allEntries = true)
	public Feature createFeature(Feature feature) {
		log.info("Creando nueva característica: {}", feature.getTitle());
		return featureRepository.save(feature);
	}

	@CacheEvict(value = "activeFeatures", allEntries = true)
	public Feature updateFeature(String id, Feature featureDetails) {
		log.info("Actualizando característica con ID: {}", id);

		Feature feature = featureRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Característica no encontrada: " + id));

		feature.setTitle(featureDetails.getTitle());
		feature.setDescription(featureDetails.getDescription());
		feature.setIcon(featureDetails.getIcon());
		feature.setIsActive(featureDetails.getIsActive());

		return featureRepository.save(feature);
	}

	@CacheEvict(value = "activeFeatures", allEntries = true)
	public void deleteFeature(String id) {
		log.info("Eliminando característica con ID: {}", id);
		featureRepository.deleteById(id);
	}

	// ================= TESTIMONIALS =================

	@Cacheable("activeTestimonials")
	@Transactional(readOnly = true)
	public List<Testimonial> getActiveTestimonials() {
		return testimonialRepository.findByIsActiveTrueOrderByName();
	}

	@Transactional(readOnly = true)
	public Page<Testimonial> getAllTestimonials(Pageable pageable) {
		return testimonialRepository.findAll(pageable);
	}

	@Transactional(readOnly = true)
	public Optional<Testimonial> getTestimonialById(String id) {
		return testimonialRepository.findById(id);
	}

	@CacheEvict(value = "activeTestimonials", allEntries = true)
	public Testimonial createTestimonial(Testimonial testimonial) {
		log.info("Creando nuevo testimonio de: {}", testimonial.getName());
		return testimonialRepository.save(testimonial);
	}

	@CacheEvict(value = "activeTestimonials", allEntries = true)
	public Testimonial updateTestimonial(String id, Testimonial testimonialDetails) {
		log.info("Actualizando testimonio con ID: {}", id);

		Testimonial testimonial = testimonialRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Testimonio no encontrado: " + id));

		testimonial.setName(testimonialDetails.getName());
		testimonial.setRole(testimonialDetails.getRole());
		testimonial.setContent(testimonialDetails.getContent());
		testimonial.setRating(testimonialDetails.getRating());
		testimonial.setIsActive(testimonialDetails.getIsActive());

		return testimonialRepository.save(testimonial);
	}

	@CacheEvict(value = "activeTestimonials", allEntries = true)
	public void deleteTestimonial(String id) {
		log.info("Eliminando testimonio con ID: {}", id);
		testimonialRepository.deleteById(id);
	}

	// ================= BANNERS =================

	@Cacheable("activeBanners")
	@Transactional(readOnly = true)
	public List<Banner> getActiveBanners() {
		return bannerRepository.findByIsActiveTrueOrderByPriorityAsc();
	}

	@Transactional(readOnly = true)
	public Page<Banner> getAllBanners(Pageable pageable) {
		return bannerRepository.findAll(pageable);
	}

	@Transactional(readOnly = true)
	public Optional<Banner> getBannerById(String id) {
		return bannerRepository.findById(id);
	}

	@CacheEvict(value = "activeBanners", allEntries = true)
	public Banner createBanner(Banner banner) {
		log.info("Creando nuevo banner: {}", banner.getTitle());
		return bannerRepository.save(banner);
	}

	@CacheEvict(value = "activeBanners", allEntries = true)
	public Banner updateBanner(String id, Banner bannerDetails) {
		log.info("Actualizando banner con ID: {}", id);

		Banner banner = bannerRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Banner no encontrado: " + id));

		banner.setTitle(bannerDetails.getTitle());
		banner.setDescription(bannerDetails.getDescription());
		banner.setButtonText(bannerDetails.getButtonText());
		banner.setButtonUrl(bannerDetails.getButtonUrl());
		banner.setBackgroundColor(bannerDetails.getBackgroundColor());
		banner.setTextColor(bannerDetails.getTextColor());
		banner.setIsActive(bannerDetails.getIsActive());
		banner.setPriority(bannerDetails.getPriority());

		return bannerRepository.save(banner);
	}

	@CacheEvict(value = "activeBanners", allEntries = true)
	public void deleteBanner(String id) {
		log.info("Eliminando banner con ID: {}", id);
		bannerRepository.deleteById(id);
	}

	// ================= EMAIL TEMPLATES =================

	@Transactional(readOnly = true)
	public List<EmailTemplate> getAllEmailTemplates() {
		return emailTemplateRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Optional<EmailTemplate> getEmailTemplateById(String id) {
		return emailTemplateRepository.findById(id);
	}

	@Transactional(readOnly = true)
	public Optional<EmailTemplate> getEmailTemplateByType(EmailTemplate.TemplateType type) {
    return emailTemplateRepository.findByTypeAndIsActiveTrue(type);
}

	public EmailTemplate createEmailTemplate(EmailTemplate template) {
		log.info("Creando nueva plantilla de email: {}", template.getName());
		return emailTemplateRepository.save(template);
	}

	public EmailTemplate updateEmailTemplate(String id, EmailTemplate templateDetails) {
		log.info("Actualizando plantilla de email con ID: {}", id);

		EmailTemplate template = emailTemplateRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Plantilla de email no encontrada: " + id));

		template.setName(templateDetails.getName());
		template.setSubject(templateDetails.getSubject());
		template.setHtmlContent(templateDetails.getHtmlContent());
		template.setType(templateDetails.getType());
		template.setIsActive(templateDetails.getIsActive());

		return emailTemplateRepository.save(template);
	}

	public void deleteEmailTemplate(String id) {
		log.info("Eliminando plantilla de email con ID: {}", id);
		emailTemplateRepository.deleteById(id);
	}

	// ================= MÉTODOS PRIVADOS =================

	private AppSettings createDefaultSettings() {
		log.info("Creando configuraciones por defecto");

		AppSettings defaultSettings = AppSettings.builder()
				.siteName("JCE Consulta API")
				.siteDescription("Servicio de consulta de cédulas dominicanas")
				.heroTitle("Consulta tu Cédula")
				.heroSubtitle("Obtén información actualizada de la JCE")
				.tokenPrice(new BigDecimal("5.00"))
				.build();

		return appSettingsRepository.save(defaultSettings);
	}
}