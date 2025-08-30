package com.arojas.jce_consulta_api.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.arojas.jce_consulta_api.entity.EmailTemplate;
import com.arojas.jce_consulta_api.entity.EmailTemplate.TemplateType;
import com.arojas.jce_consulta_api.entity.PaymentOrder;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para envío de emails usando Spring Boot Mail
 * Solo usa plantillas de la base de datos, no contiene HTML embebido.
 * 
 * @author arojas
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

	private final JavaMailSender mailSender;
	private final AppSettingsService appSettingsService;

	@Value("${app.email.from:noreply@jce-consulta.com}")
	private String fromEmail;

	@Value("${app.email.enabled:true}")
	private boolean emailEnabled;

	@Value("${app.name:JCE Consulta}")
	private String appName;

	// ================= MÉTODOS PÚBLICOS =================

	@Async
	public CompletableFuture<Void> sendWelcomeEmail(String userEmail, String userName) {
		return sendEmailFromTemplate(userEmail, TemplateType.WELCOME, Map.of(
				"userName", userName,
				"appName", appName));
	}

	@Async
	public CompletableFuture<Void> sendLoginNotificationEmail(String userEmail, String userName, String ipAddress,
			String deviceInfo) {
		return sendEmailFromTemplate(userEmail, TemplateType.LOGIN, Map.of(
				"userName", userName,
				"appName", appName,
				"ipAddress", ipAddress != null ? ipAddress : "Desconocida",
				"deviceInfo", deviceInfo != null ? deviceInfo : "Desconocido",
				"loginDateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
	}

	@Async
	public CompletableFuture<Void> sendPaymentConfirmationEmail(String userEmail, PaymentOrder paymentOrder) {
		LocalDateTime paymentDate = paymentOrder.getUpdatedAt() != null ? paymentOrder.getUpdatedAt()
				: LocalDateTime.now();
		return sendEmailFromTemplate(userEmail, TemplateType.PAYMENT_CONFIRMED, Map.of(
				"appName", appName,
				"orderId", paymentOrder.getId(),
				"tokens", String.valueOf(paymentOrder.getTokens()),
				"amount", paymentOrder.getAmount(),
				"paymentDate", paymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
	}

	@Async
	public CompletableFuture<Void> sendAdminNotificationEmail(String adminEmail, String message) {
		return sendEmailFromTemplate(adminEmail, TemplateType.ADMIN_LOGIN, Map.of(
				"appName", appName,
				"message", message,
				"notificationTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
	}

	@Async
	public CompletableFuture<Void> sendCustomEmail(String userEmail, String subject, String content) {
		return CompletableFuture.runAsync(() -> {
			try {
				log.info("Enviando email personalizado a: {}", userEmail);
				sendHtmlEmail(userEmail, subject, content);
				log.info("Email personalizado enviado exitosamente a: {}", userEmail);
			} catch (Exception e) {
				log.error("Error enviando email personalizado a {}: {}", userEmail, e.getMessage(), e);
			}
		});
	}

	public boolean isEmailEnabled() {
		return emailEnabled;
	}

	public void sendTestEmail(String testEmail) {
		sendEmailFromTemplate(testEmail, TemplateType.WELCOME, Map.of(
				"appName", appName,
				"testTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
	}

	// ================= MÉTODOS PRIVADOS =================

	private CompletableFuture<Void> sendEmailFromTemplate(String to, TemplateType templateType,
			Map<String, ?> variables) {
		return CompletableFuture.runAsync(() -> {
			if (!emailEnabled) {
				log.info("Emails deshabilitados - no se envió email a: {}", to);
				return;
			}

			try {
				EmailTemplate template = appSettingsService.getEmailTemplateByType(templateType)
						.orElseThrow(() -> new RuntimeException("No se encontró plantilla para tipo: " + templateType));

				String subject = replaceVariables(template.getSubject(), variables);
				String content = replaceVariables(template.getHtmlContent(), variables);

				sendHtmlEmail(to, subject, content);
				log.info("Email de tipo {} enviado exitosamente a: {}", templateType, to);

			} catch (Exception e) {
				log.error("Error enviando email de tipo {} a {}: {}", templateType, to, e.getMessage(), e);
			}
		});
	}

	private void sendHtmlEmail(String to, String subject, String content) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(content, true); // true = HTML enabled
			mailSender.send(message);
		} catch (MessagingException e) {
			log.error("Error enviando email a {}: {}", to, e.getMessage(), e);
			throw new RuntimeException("Error enviando email", e);
		}
	}

	private String replaceVariables(String content, Map<String, ?> variables) {
		if (content == null)
			return "";
		String result = content;
		for (Map.Entry<String, ?> entry : variables.entrySet()) {
			result = result.replace("{{" + entry.getKey() + "}}",
					entry.getValue() != null ? entry.getValue().toString() : "");
		}
		return result;
	}
}