/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author arojas
 */

@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@AllArgsConstructor
public class RefreshToken {

	@Id
	@Column(name = "id", columnDefinition = "VARCHAR(36)")
	private String id;

	@Column(name = "token", nullable = false, unique = true, columnDefinition = "TEXT")
	private String token;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiryDate;

	@Column(name = "is_revoked", nullable = false)
	private Boolean isRevoked = false;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	// Constructors
	public RefreshToken() {
		this.id = UUID.randomUUID().toString();
	}

	public RefreshToken(String token, User user, LocalDateTime expiryDate) {
		this();
		this.token = token;
		this.user = user;
		this.expiryDate = expiryDate;
	}

	// Getters and Setters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LocalDateTime getexpiryDate() {
		return expiryDate;
	}

	public void setexpiryDate(LocalDateTime expiryDate) {
		this.expiryDate = expiryDate;
	}

	public Boolean getIsRevoked() {
		return isRevoked;
	}

	public void setIsRevoked(Boolean isRevoked) {
		this.isRevoked = isRevoked;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	// Utility methods
	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiryDate);
	}

	public boolean isValid() {
		return !isRevoked && !isExpired();
	}

	public void revoke() {
		this.isRevoked = true;
	}
}