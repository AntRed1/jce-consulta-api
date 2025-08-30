/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author arojas
 */

@Data
@Builder
@AllArgsConstructor
@Entity
@Table(name = "app_settings")
public class AppSettings {

	@Id
	@Column(name = "id", columnDefinition = "VARCHAR(36)")
	private String id;

	@Column(name = "site_name", nullable = false)
	private String siteName;

	@Column(name = "site_description", columnDefinition = "TEXT")
	private String siteDescription;

	@Column(name = "hero_title", nullable = false)
	private String heroTitle;

	@Column(name = "hero_subtitle", columnDefinition = "TEXT")
	private String heroSubtitle;

	@Column(name = "token_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal tokenPrice;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "appSettings", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Feature> features;

	@OneToMany(mappedBy = "appSettings", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Testimonial> testimonials;

	@OneToMany(mappedBy = "appSettings", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Banner> banners;

	@OneToMany(mappedBy = "appSettings", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<EmailTemplate> emailTemplates;

	// Constructors
	public AppSettings() {
		this.id = UUID.randomUUID().toString();
	}

	// Getters and Setters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getSiteDescription() {
		return siteDescription;
	}

	public void setSiteDescription(String siteDescription) {
		this.siteDescription = siteDescription;
	}

	public String getHeroTitle() {
		return heroTitle;
	}

	public void setHeroTitle(String heroTitle) {
		this.heroTitle = heroTitle;
	}

	public String getHeroSubtitle() {
		return heroSubtitle;
	}

	public void setHeroSubtitle(String heroSubtitle) {
		this.heroSubtitle = heroSubtitle;
	}

	public BigDecimal getTokenPrice() {
		return tokenPrice;
	}

	public void setTokenPrice(BigDecimal tokenPrice) {
		this.tokenPrice = tokenPrice;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<Feature> getFeatures() {
		return features;
	}

	public void setFeatures(List<Feature> features) {
		this.features = features;
	}

	public List<Testimonial> getTestimonials() {
		return testimonials;
	}

	public void setTestimonials(List<Testimonial> testimonials) {
		this.testimonials = testimonials;
	}

	public List<Banner> getBanners() {
		return banners;
	}

	public void setBanners(List<Banner> banners) {
		this.banners = banners;
	}

	public List<EmailTemplate> getEmailTemplates() {
		return emailTemplates;
	}

	public void setEmailTemplates(List<EmailTemplate> emailTemplates) {
		this.emailTemplates = emailTemplates;
	}
}