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

	/**
	 * Envía email de bienvenida al usuario
	 */
	@Async
	public CompletableFuture<Void> sendWelcomeEmail(String userEmail, String userName) {
		return CompletableFuture.runAsync(() -> {
			try {
				log.info("Enviando email de bienvenida a: {}", userEmail);

				// Intentar obtener plantilla personalizada, sino usar la por defecto
				EmailTemplate template = appSettingsService.getEmailTemplateByType(TemplateType.WELCOME)
						.orElse(getDefaultWelcomeTemplate());

				String subject = replaceVariables(template.getSubject(), Map.of(
						"userName", userName,
						"appName", appName));

				String htmlContent = replaceVariables(template.getHtmlContent(), Map.of(
						"userName", userName,
						"appName", appName));

				sendHtmlEmail(userEmail, subject, htmlContent);
				log.info("Email de bienvenida enviado exitosamente a: {}", userEmail);

			} catch (Exception e) {
				log.error("Error enviando email de bienvenida a {}: {}", userEmail, e.getMessage(), e);
			}
		});
	}

	/**
	 * Envía email de confirmación de login
	 */
	@Async
	public CompletableFuture<Void> sendLoginNotificationEmail(String userEmail, String userName, String ipAddress) {
		return CompletableFuture.runAsync(() -> {
			try {
				log.info("Enviando notificación de login a: {}", userEmail);

				EmailTemplate template = appSettingsService.getEmailTemplateByType(TemplateType.LOGIN)
						.orElse(getDefaultLoginTemplate());

				String subject = replaceVariables(template.getSubject(), Map.of(
						"userName", userName,
						"appName", appName));

				String htmlContent = replaceVariables(template.getHtmlContent(), Map.of(
						"userName", userName,
						"appName", appName,
						"ipAddress", ipAddress != null ? ipAddress : "Desconocida",
						"loginTime", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now())));

				sendHtmlEmail(userEmail, subject, htmlContent);
				log.info("Notificación de login enviada exitosamente a: {}", userEmail);

			} catch (Exception e) {
				log.error("Error enviando notificación de login a {}: {}", userEmail, e.getMessage(), e);
			}
		});
	}

	/**
	 * Envía email de confirmación de pago
	 */
	@Async
	public CompletableFuture<Void> sendPaymentConfirmationEmail(String userEmail, PaymentOrder paymentOrder) {
		return CompletableFuture.runAsync(() -> {
			try {
				log.info("Enviando confirmación de pago a: {} - orden: {}", userEmail, paymentOrder.getId());

				EmailTemplate template = appSettingsService.getEmailTemplateByType(TemplateType.PAYMENT_CONFIRMED)
						.orElse(getDefaultPaymentTemplate());

				String subject = replaceVariables(template.getSubject(), Map.of(
						"appName", appName,
						"orderId", paymentOrder.getId()));

				// Obtener fecha de pago (usar updatedAt si completedAt no existe)
				LocalDateTime paymentDate = paymentOrder.getUpdatedAt() != null
						? paymentOrder.getUpdatedAt()
						: LocalDateTime.now();

				String htmlContent = replaceVariables(template.getHtmlContent(), Map.of(
						"appName", appName,
						"orderId", paymentOrder.getId(),
						"tokens", String.valueOf(paymentOrder.getTokens()),
						"amount", paymentOrder.getAmount(),
						"paymentDate", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(paymentDate)));

				sendHtmlEmail(userEmail, subject, htmlContent);
				log.info("Confirmación de pago enviada exitosamente a: {}", userEmail);

			} catch (Exception e) {
				log.error("Error enviando confirmación de pago a {}: {}", userEmail, e.getMessage(), e);
			}
		});
	}

	/**
	 * Envía email de notificación a administradores
	 */
	@Async
	public CompletableFuture<Void> sendAdminNotificationEmail(String adminEmail, String subject, String message) {
		return CompletableFuture.runAsync(() -> {
			try {
				log.info("Enviando notificación de admin a: {}", adminEmail);

				String htmlContent = getAdminNotificationTemplate(message);
				sendHtmlEmail(adminEmail, subject, htmlContent);

				log.info("Notificación de admin enviada exitosamente a: {}", adminEmail);

			} catch (Exception e) {
				log.error("Error enviando notificación de admin a {}: {}", adminEmail, e.getMessage(), e);
			}
		});
	}

	/**
	 * Envía email personalizado
	 */
	@Async
	public CompletableFuture<Void> sendCustomEmail(String userEmail, String subject, String htmlContent) {
		return CompletableFuture.runAsync(() -> {
			try {
				log.info("Enviando email personalizado a: {}", userEmail);
				sendHtmlEmail(userEmail, subject, htmlContent);
				log.info("Email personalizado enviado exitosamente a: {}", userEmail);

			} catch (Exception e) {
				log.error("Error enviando email personalizado a {}: {}", userEmail, e.getMessage(), e);
			}
		});
	}

	/**
	 * Verifica si el servicio de email está habilitado
	 */
	public boolean isEmailEnabled() {
		return emailEnabled;
	}

	/**
	 * Envía email de prueba para verificar configuración
	 */
	public void sendTestEmail(String testEmail) {
		try {
			String subject = "Email de Prueba - " + appName;
			String htmlContent = getTestEmailTemplate();
			sendHtmlEmail(testEmail, subject, htmlContent);
			log.info("Email de prueba enviado a: {}", testEmail);
		} catch (Exception e) {
			log.error("Error enviando email de prueba: {}", e.getMessage(), e);
			throw new RuntimeException("Error en configuración de email", e);
		}
	}

	// ================= MÉTODOS PRIVADOS =================

	private void sendHtmlEmail(String to, String subject, String htmlContent) {
		if (!emailEnabled) {
			log.info("Emails deshabilitados - no se envió email a: {}", to);
			return;
		}

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(htmlContent, true);

			mailSender.send(message);
			log.debug("Email enviado exitosamente a: {}", to);

		} catch (MessagingException e) {
			log.error("Error enviando email a {}: {}", to, e.getMessage(), e);
			throw new RuntimeException("Error enviando email", e);
		}
	}

	private String replaceVariables(String content, Map<String, ?> variables) {
		String result = content;
		for (Map.Entry<String, ?> entry : variables.entrySet()) {
			result = result.replace("{{" + entry.getKey() + "}}",
					entry.getValue() != null ? entry.getValue().toString() : "");
		}
		return result;
	}

	// ================= PLANTILLAS POR DEFECTO =================

	private EmailTemplate getDefaultWelcomeTemplate() {
		EmailTemplate template = new EmailTemplate();
		template.setName("Welcome Default");
		template.setSubject("¡Bienvenido a {{appName}}!");
		template.setType(TemplateType.WELCOME);
		template.setHtmlContent(
				"""
						<!DOCTYPE html>
						<html>
						<head>
						    <meta charset="UTF-8">
						    <meta name="viewport" content="width=device-width, initial-scale=1.0">
						    <style>
						        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
						        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
						        .header { background: #007bff; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
						        .content { padding: 20px; background: #f8f9fa; border-radius: 0 0 8px 8px; }
						        .footer { background: #6c757d; color: white; padding: 10px; text-align: center; font-size: 12px; margin-top: 20px; border-radius: 8px; }
						        ul { padding-left: 20px; }
						        li { margin-bottom: 8px; }
						    </style>
						</head>
						<body>
						    <div class="container">
						        <div class="header">
						            <h1>¡Bienvenido a {{appName}}!</h1>
						        </div>
						        <div class="content">
						            <h2>Hola {{userName}},</h2>
						            <p>¡Gracias por registrarte en {{appName}}! Ahora puedes realizar consultas de cédulas dominicanas de forma rápida y segura.</p>
						            <p><strong>¿Qué puedes hacer ahora?</strong></p>
						            <ul>
						                <li>Comprar tokens para realizar consultas</li>
						                <li>Consultar información de cédulas dominicanas</li>
						                <li>Ver tu historial de consultas</li>
						            </ul>
						            <p>¡Esperamos que disfrutes de nuestro servicio!</p>
						        </div>
						        <div class="footer">
						            <p>&copy; 2024 {{appName}}. Todos los derechos reservados.</p>
						        </div>
						    </div>
						</body>
						</html>
						""");
		return template;
	}

	private EmailTemplate getDefaultLoginTemplate() {
		EmailTemplate template = new EmailTemplate();
		template.setName("Login Default");
		template.setSubject("Nuevo acceso a tu cuenta - {{appName}}");
		template.setType(TemplateType.LOGIN);
		template.setHtmlContent(
				"""
						<!DOCTYPE html>
						<html>
						<head>
						    <meta charset="UTF-8">
						    <meta name="viewport" content="width=device-width, initial-scale=1.0">
						    <style>
						        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
						        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
						        .header { background: #28a745; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
						        .content { padding: 20px; background: #f8f9fa; border-radius: 0 0 8px 8px; }
						        .info-box { background: #e9ecef; padding: 15px; border-radius: 5px; margin: 10px 0; }
						        .footer { background: #6c757d; color: white; padding: 10px; text-align: center; font-size: 12px; margin-top: 20px; border-radius: 8px; }
						    </style>
						</head>
						<body>
						    <div class="container">
						        <div class="header">
						            <h1>Acceso a tu cuenta</h1>
						        </div>
						        <div class="content">
						            <h2>Hola {{userName}},</h2>
						            <p>Se ha detectado un nuevo acceso a tu cuenta de {{appName}}.</p>
						            <div class="info-box">
						                <strong>Detalles del acceso:</strong><br>
						                Fecha y hora: {{loginTime}}<br>
						                Dirección IP: {{ipAddress}}
						            </div>
						            <p>Si no fuiste tú quien accedió, te recomendamos cambiar tu contraseña inmediatamente.</p>
						        </div>
						        <div class="footer">
						            <p>&copy; 2024 {{appName}}. Todos los derechos reservados.</p>
						        </div>
						    </div>
						</body>
						</html>
						""");
		return template;
	}

	private EmailTemplate getDefaultPaymentTemplate() {
		EmailTemplate template = new EmailTemplate();
		template.setName("Payment Default");
		template.setSubject("¡Pago confirmado! - {{appName}}");
		template.setType(TemplateType.PAYMENT_CONFIRMED);
		template.setHtmlContent(
				"""
						<!DOCTYPE html>
						<html>
						<head>
						    <meta charset="UTF-8">
						    <meta name="viewport" content="width=device-width, initial-scale=1.0">
						    <style>
						        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
						        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
						        .header { background: #28a745; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
						        .content { padding: 20px; background: #f8f9fa; border-radius: 0 0 8px 8px; }
						        .payment-details { background: #d1edff; padding: 15px; border-radius: 5px; margin: 15px 0; }
						        .footer { background: #6c757d; color: white; padding: 10px; text-align: center; font-size: 12px; margin-top: 20px; border-radius: 8px; }
						    </style>
						</head>
						<body>
						    <div class="container">
						        <div class="header">
						            <h1>¡Pago Confirmado!</h1>
						        </div>
						        <div class="content">
						            <h2>¡Gracias por tu compra!</h2>
						            <p>Tu pago ha sido procesado exitosamente y los tokens han sido agregados a tu cuenta.</p>
						            <div class="payment-details">
						                <h3>Detalles de la compra:</h3>
						                <p><strong>Orden ID:</strong> {{orderId}}</p>
						                <p><strong>Tokens comprados:</strong> {{tokens}}</p>
						                <p><strong>Monto pagado:</strong> ${{amount}}</p>
						                <p><strong>Fecha de pago:</strong> {{paymentDate}}</p>
						            </div>
						            <p>Ahora puedes usar tus tokens para realizar consultas de cédulas dominicanas.</p>
						            <p><strong>Recuerda:</strong> Los tokens tienen una validez de 24 horas y solo se pueden usar una vez por consulta.</p>
						        </div>
						        <div class="footer">
						            <p>&copy; 2024 {{appName}}. Todos los derechos reservados.</p>
						        </div>
						    </div>
						</body>
						</html>
						""");
		return template;
	}

	private String getAdminNotificationTemplate(String message) {
		return """
				<!DOCTYPE html>
				<html>
				<head>
				    <meta charset="UTF-8">
				    <meta name="viewport" content="width=device-width, initial-scale=1.0">
				    <style>
				        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
				        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
				        .header { background: #dc3545; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
				        .content { padding: 20px; background: #f8f9fa; border-radius: 0 0 8px 8px; }
				        .message { background: #fff3cd; padding: 15px; border-radius: 5px; border-left: 4px solid #ffc107; }
				        .footer { background: #6c757d; color: white; padding: 10px; text-align: center; font-size: 12px; margin-top: 20px; border-radius: 8px; }
				    </style>
				</head>
				<body>
				    <div class="container">
				        <div class="header">
				            <h1>Notificación de Administrador</h1>
				        </div>
				        <div class="content">
				            <h2>Notificación del Sistema</h2>
				            <div class="message">
				                <p>%s</p>
				            </div>
				            <p><strong>Fecha:</strong> %s</p>
				        </div>
				        <div class="footer">
				            <p>&copy; 2024 %s. Sistema de notificaciones.</p>
				        </div>
				    </div>
				</body>
				</html>
				"""
				.formatted(message, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
						appName);
	}

	private String getTestEmailTemplate() {
		return """
				<!DOCTYPE html>
				<html>
				<head>
				    <meta charset="UTF-8">
				    <meta name="viewport" content="width=device-width, initial-scale=1.0">
				    <style>
				        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
				        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
				        .header { background: #007bff; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
				        .content { padding: 20px; background: #f8f9fa; border-radius: 0 0 8px 8px; }
				        .footer { background: #6c757d; color: white; padding: 10px; text-align: center; font-size: 12px; margin-top: 20px; border-radius: 8px; }
				        .success { background: #d4edda; padding: 15px; border-radius: 5px; border-left: 4px solid #28a745; color: #155724; }
				    </style>
				</head>
				<body>
				    <div class="container">
				        <div class="header">
				            <h1>Email de Prueba</h1>
				        </div>
				        <div class="content">
				            <div class="success">
				                <h2>¡Configuración exitosa!</h2>
				                <p>Si recibes este email, significa que la configuración de correo de %s está funcionando correctamente.</p>
				            </div>
				            <p><strong>Detalles de la prueba:</strong></p>
				            <ul>
				                <li>Fecha de envío: %s</li>
				                <li>Sistema: %s</li>
				                <li>Estado: Operativo</li>
				            </ul>
				        </div>
				        <div class="footer">
				            <p>&copy; 2024 %s. Email de prueba.</p>
				        </div>
				    </div>
				</body>
				</html>
				"""
				.formatted(appName, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
						appName, appName);
	}
}