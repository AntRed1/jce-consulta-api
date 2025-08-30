package com.arojas.jce_consulta_api.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "payment_orders")
@Data
@Builder
@AllArgsConstructor
public class PaymentOrder {

	@Id
	@Column(name = "id", columnDefinition = "VARCHAR(36)")
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "tokens", nullable = false)
	private Integer tokens;

	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private PaymentStatus status = PaymentStatus.PENDING;

	@Column(name = "buy_me_coffee_url", nullable = false)
	private String buyMeCoffeeUrl;

	@Column(name = "external_reference")
	private String externalReference;

	@Column(name = "payment_method")
	private String paymentMethod;

	@Column(name = "payment_date")
	private LocalDateTime paymentDate;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@Column(name = "error_message")
	private String errorMessage;

	@Column(name = "webhook_received", nullable = false)
	private Boolean webhookReceived = false;

	@Column(name = "notes", columnDefinition = "TEXT")
	private String notes;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// Constructors
	public PaymentOrder() {
		this.id = UUID.randomUUID().toString();
	}

	public PaymentOrder(User user, Integer tokens, BigDecimal amount, String buyMeCoffeeUrl) {
		this();
		this.user = user;
		this.tokens = tokens;
		this.amount = amount;
		this.buyMeCoffeeUrl = buyMeCoffeeUrl;
	}

	// Getters and Setters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getTokens() {
		return tokens;
	}

	public void setTokens(Integer tokens) {
		this.tokens = tokens;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
	}

	public String getBuyMeCoffeeUrl() {
		return buyMeCoffeeUrl;
	}

	public void setBuyMeCoffeeUrl(String buyMeCoffeeUrl) {
		this.buyMeCoffeeUrl = buyMeCoffeeUrl;
	}

	public String getExternalReference() {
		return externalReference;
	}

	public void setExternalReference(String externalReference) {
		this.externalReference = externalReference;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public LocalDateTime getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(LocalDateTime paymentDate) {
		this.paymentDate = paymentDate;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Boolean getWebhookReceived() {
		return webhookReceived;
	}

	public void setWebhookReceived(Boolean webhookReceived) {
		this.webhookReceived = webhookReceived;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
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

	// Utility methods
	public void markAsCompleted(String externalReference, String paymentMethod) {
		this.status = PaymentStatus.COMPLETED;
		this.externalReference = externalReference;
		this.paymentMethod = paymentMethod;
		this.paymentDate = LocalDateTime.now();
		this.completedAt = LocalDateTime.now();
		this.webhookReceived = true;
	}

	public void markAsFailed(String reason) {
		this.status = PaymentStatus.FAILED;
		this.errorMessage = reason;
		this.completedAt = LocalDateTime.now();
	}

	public boolean isPending() {
		return this.status == PaymentStatus.PENDING;
	}

	public boolean isCompleted() {
		return this.status == PaymentStatus.COMPLETED;
	}

	public boolean isFailed() {
		return this.status == PaymentStatus.FAILED;
	}

	public enum PaymentStatus {
		PENDING, COMPLETED, FAILED
	}
}