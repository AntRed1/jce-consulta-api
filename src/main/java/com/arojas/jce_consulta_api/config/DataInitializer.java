package com.arojas.jce_consulta_api.config;

import com.arojas.jce_consulta_api.entity.*;
import com.arojas.jce_consulta_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Inicializador de datos para la aplicación JCE Consulta
 * Inserta datos iniciales en las tablas: banners, email_templates, features,
 * testimonials, y configura AppSettings
 * 
 * @author arojas
 */
@Component
public class DataInitializer implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

	@Autowired
	private AppSettingsRepository appSettingsRepository;

	@Autowired
	private BannerRepository bannerRepository;

	@Autowired
	private EmailTemplateRepository emailTemplateRepository;

	@Autowired
	private FeatureRepository featureRepository;

	@Autowired
	private TestimonialRepository testimonialRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void run(ApplicationArguments args) throws Exception {
		logger.info("Iniciando inicialización de datos...");

		try {
			// Verificar y crear AppSettings si no existe
			AppSettings appSettings = initializeAppSettings();

			// Inicializar datos si las tablas están vacías
			initializeBanners(appSettings);
			initializeEmailTemplates(appSettings);
			initializeFeatures(appSettings);
			initializeTestimonials(appSettings);
			initializeAdminUser();

			logger.info("Inicialización de datos completada exitosamente.");

		} catch (Exception e) {
			logger.error("Error durante la inicialización de datos: {}", e.getMessage(), e);
			throw e;
		}
	}

	private void initializeAdminUser() {
		String adminEmail = "anthonyatras@gmail.com";
		if (userRepository.findByEmail(adminEmail).isEmpty()) {
			logger.info("Creando usuario ADMIN por defecto...");

			User admin = new User();
			admin.setId(UUID.randomUUID().toString());
			admin.setName("Administrador Root");
			admin.setEmail(adminEmail);
			admin.setPassword(passwordEncoder.encode("Emulador1"));
			admin.setRole(User.Role.ADMIN);
			admin.setTokens(100);
			admin.setIsActive(true);
			admin.setCreatedAt(LocalDateTime.now());
			admin.setUpdatedAt(LocalDateTime.now());

			userRepository.save(admin);
			logger.info("Usuario ADMIN creado con email: {}", adminEmail);
		} else {
			logger.info("Usuario ADMIN ya existe con email: {}", adminEmail);
		}
	}

	/**
	 * Inicializa AppSettings de manera segura
	 */
	private AppSettings initializeAppSettings() {
		logger.info("Verificando configuraciones de AppSettings...");

		Optional<AppSettings> existingSettings = appSettingsRepository.findFirstByOrderByCreatedAtAsc();

		if (existingSettings.isPresent()) {
			logger.info("Configuraciones de AppSettings ya existen");
			return existingSettings.get();
		}

		logger.info("Creando configuraciones por defecto para AppSettings");
		return createDefaultSettings();
	}

	/**
	 * Crea configuraciones por defecto para AppSettings
	 */
	private AppSettings createDefaultSettings() {
		logger.info("Creando configuraciones por defecto para AppSettings");
		LocalDateTime now = LocalDateTime.now();
		String settingsId = UUID.randomUUID().toString();

		AppSettings defaultSettings = new AppSettings();
		defaultSettings.setId(settingsId);
		defaultSettings.setSiteName("JCE Consulta API");
		defaultSettings.setSiteDescription("Servicio de consulta de cédulas dominicanas");
		defaultSettings.setHeroTitle("Consulta tu Cédula");
		defaultSettings.setHeroSubtitle("Obtén información actualizada de la JCE");
		defaultSettings.setTokenPrice(new BigDecimal("5.00"));
		defaultSettings.setIsActive(true);
		defaultSettings.setCreatedAt(now);
		defaultSettings.setUpdatedAt(now);

		try {
			AppSettings savedSettings = appSettingsRepository.save(defaultSettings);
			logger.info("AppSettings creado exitosamente con ID: {}", savedSettings.getId());
			return savedSettings;
		} catch (Exception e) {
			logger.error("Error al crear AppSettings por defecto: {}", e.getMessage());
			// Intentar buscar de nuevo por si otro proceso lo creó
			Optional<AppSettings> retrySettings = appSettingsRepository.findFirstByOrderByCreatedAtAsc();
			if (retrySettings.isPresent()) {
				logger.info("AppSettings encontrado en segundo intento");
				return retrySettings.get();
			}
			throw e;
		}
	}

	/**
	 * Inicializa los banners por defecto
	 */
	private void initializeBanners(AppSettings appSettings) {
		if (bannerRepository.count() == 0) {
			logger.info("Inicializando banners...");

			Banner mainBanner = new Banner();
			mainBanner.setAppSettings(appSettings);
			mainBanner.setTitle("Bienvenido a JCE Consulta");
			mainBanner.setDescription(
					"Tu plataforma de consultas profesionales. Conectamos a profesionales con clientes que necesitan servicios de consultoría especializados.");
			mainBanner.setButtonText("Explorar Servicios");
			mainBanner.setButtonUrl("/services");
			mainBanner.setBackgroundColor("#2c3e50");
			mainBanner.setTextColor("#ffffff");
			mainBanner.setPriority(1);
			mainBanner.setIsActive(true);
			mainBanner.setCreatedAt(LocalDateTime.now());
			mainBanner.setUpdatedAt(LocalDateTime.now());

			Banner servicesBanner = new Banner();
			servicesBanner.setAppSettings(appSettings);
			servicesBanner.setTitle("Servicios Profesionales de Consultoría");
			servicesBanner.setDescription(
					"Ofrecemos servicios de consultoría especializados en diferentes áreas profesionales con expertos certificados y experiencia comprobada.");
			servicesBanner.setButtonText("Ver Servicios");
			servicesBanner.setButtonUrl("/services");
			servicesBanner.setBackgroundColor("#27ae60");
			servicesBanner.setTextColor("#ffffff");
			servicesBanner.setPriority(2);
			servicesBanner.setIsActive(true);
			servicesBanner.setCreatedAt(LocalDateTime.now());
			servicesBanner.setUpdatedAt(LocalDateTime.now());

			Banner contactBanner = new Banner();
			contactBanner.setAppSettings(appSettings);
			contactBanner.setTitle("¿Necesitas una Consulta Especializada?");
			contactBanner.setDescription(
					"Nuestro equipo de expertos está listo para ayudarte con tus proyectos y consultas. Contáctanos hoy mismo y obtén la orientación que necesitas.");
			contactBanner.setButtonText("Contáctanos");
			contactBanner.setButtonUrl("/contact");
			contactBanner.setBackgroundColor("#3498db");
			contactBanner.setTextColor("#ffffff");
			contactBanner.setPriority(3);
			contactBanner.setIsActive(true);
			contactBanner.setCreatedAt(LocalDateTime.now());
			contactBanner.setUpdatedAt(LocalDateTime.now());

			bannerRepository.save(mainBanner);
			bannerRepository.save(servicesBanner);
			bannerRepository.save(contactBanner);

			logger.info("Banners inicializados correctamente: 3 registros creados");
		} else {
			logger.info("Los banners ya están inicializados");
		}
	}

	/**
	 * Inicializa las plantillas de email por defecto
	 */
	private void initializeEmailTemplates(AppSettings appSettings) {
		if (emailTemplateRepository.count() == 0) {
			logger.info("Inicializando plantillas de email...");

			EmailTemplate welcomeTemplate = new EmailTemplate();
			welcomeTemplate.setAppSettings(appSettings);
			welcomeTemplate.setName("Plantilla de Bienvenida");
			welcomeTemplate.setSubject("¡Bienvenido a JCE Consulta!");
			welcomeTemplate.setHtmlContent(buildWelcomeEmailTemplate());
			welcomeTemplate.setType(EmailTemplate.TemplateType.WELCOME);
			welcomeTemplate.setIsActive(true);
			welcomeTemplate.setCreatedAt(LocalDateTime.now());
			welcomeTemplate.setUpdatedAt(LocalDateTime.now());

			EmailTemplate loginTemplate = new EmailTemplate();
			loginTemplate.setAppSettings(appSettings);
			loginTemplate.setName("Notificación de Acceso");
			loginTemplate.setSubject("Nuevo acceso a tu cuenta - JCE Consulta");
			loginTemplate.setHtmlContent(buildLoginEmailTemplate());
			loginTemplate.setType(EmailTemplate.TemplateType.LOGIN);
			loginTemplate.setIsActive(true);
			loginTemplate.setCreatedAt(LocalDateTime.now());
			loginTemplate.setUpdatedAt(LocalDateTime.now());

			EmailTemplate adminLoginTemplate = new EmailTemplate();
			adminLoginTemplate.setAppSettings(appSettings);
			adminLoginTemplate.setName("Acceso de Administrador");
			adminLoginTemplate.setSubject("Acceso de administrador detectado - JCE Consulta");
			adminLoginTemplate.setHtmlContent(buildAdminLoginEmailTemplate());
			adminLoginTemplate.setType(EmailTemplate.TemplateType.ADMIN_LOGIN);
			adminLoginTemplate.setIsActive(true);
			adminLoginTemplate.setCreatedAt(LocalDateTime.now());
			adminLoginTemplate.setUpdatedAt(LocalDateTime.now());

			EmailTemplate paymentTemplate = new EmailTemplate();
			paymentTemplate.setAppSettings(appSettings);
			paymentTemplate.setName("Confirmación de Pago");
			paymentTemplate.setSubject("Pago confirmado - JCE Consulta");
			paymentTemplate.setHtmlContent(buildPaymentConfirmedEmailTemplate());
			paymentTemplate.setType(EmailTemplate.TemplateType.PAYMENT_CONFIRMED);
			paymentTemplate.setIsActive(true);
			paymentTemplate.setCreatedAt(LocalDateTime.now());
			paymentTemplate.setUpdatedAt(LocalDateTime.now());

			emailTemplateRepository.save(welcomeTemplate);
			emailTemplateRepository.save(loginTemplate);
			emailTemplateRepository.save(adminLoginTemplate);
			emailTemplateRepository.save(paymentTemplate);

			logger.info("Plantillas de email inicializadas correctamente: 4 registros creados");
		} else {
			logger.info("Las plantillas de email ya están inicializadas");
		}
	}

	/**
	 * Inicializa las características por defecto
	 */
	private void initializeFeatures(AppSettings appSettings) {
		if (featureRepository.count() == 0) {
			logger.info("Inicializando características...");

			Feature[] features = {
					createFeature(appSettings, "Consultoría Profesional",
							"Servicios de consultoría especializados con profesionales certificados en diferentes áreas.",
							"fas fa-user-tie"),
					createFeature(appSettings, "Soporte 24/7",
							"Atención continua para resolver tus dudas y consultas en cualquier momento del día.",
							"fas fa-clock"),
					createFeature(appSettings, "Seguridad Garantizada",
							"Protección completa de tus datos personales y confidencialidad en todas las consultas.",
							"fas fa-shield-alt"),
					createFeature(appSettings, "Respuesta Rápida",
							"Tiempo de respuesta optimizado para que obtengas las soluciones que necesitas sin demoras.",
							"fas fa-lightning-bolt"),
					createFeature(appSettings, "Múltiples Especialidades",
							"Amplio rango de especialidades profesionales para cubrir todas tus necesidades de consultoría.",
							"fas fa-cogs"),
					createFeature(appSettings, "Precios Competitivos",
							"Tarifas justas y transparentes que se adaptan a tu presupuesto sin comprometer la calidad.",
							"fas fa-dollar-sign")
			};

			for (Feature feature : features) {
				featureRepository.save(feature);
			}

			logger.info("Características inicializadas correctamente: {} registros creados", features.length);
		} else {
			logger.info("Las características ya están inicializadas");
		}
	}

	private Feature createFeature(AppSettings appSettings, String title, String description, String icon) {
		Feature feature = new Feature();
		feature.setAppSettings(appSettings);
		feature.setTitle(title);
		feature.setDescription(description);
		feature.setIcon(icon);
		feature.setIsActive(true);
		feature.setCreatedAt(LocalDateTime.now());
		feature.setUpdatedAt(LocalDateTime.now());
		return feature;
	}

	/**
	 * Inicializa los testimonios por defecto
	 */
	private void initializeTestimonials(AppSettings appSettings) {
		if (testimonialRepository.count() == 0) {
			logger.info("Inicializando testimonios...");

			Testimonial[] testimonials = {
					createTestimonial(appSettings, "María González", "Directora de Proyectos",
							"Excelente servicio de consultoría. Los profesionales de JCE Consulta me ayudaron a optimizar mis procesos empresariales de manera excepcional. Altamente recomendado.",
							5),
					createTestimonial(appSettings, "Carlos Rodríguez", "Gerente de Operaciones",
							"La atención al cliente es impecable y los resultados superaron mis expectativas. Definitivamente volveré a utilizar sus servicios para futuros proyectos.",
							5),
					createTestimonial(appSettings, "Ana Martínez", "Consultora Independiente",
							"Profesionalismo y calidad en cada consulta. El equipo demostró un conocimiento profundo en mi área de interés y me brindó soluciones prácticas y efectivas.",
							5),
					createTestimonial(appSettings, "Luis Hernández", "CEO StartupTech",
							"Como emprendedor, necesitaba orientación especializada. JCE Consulta me proporcionó el apoyo necesario para tomar decisiones estratégicas importantes para mi empresa.",
							4),
					createTestimonial(appSettings, "Patricia Silva", "Coordinadora de Marketing",
							"La rapidez en las respuestas y la calidad de las recomendaciones han sido fundamentales para el éxito de nuestras campañas. Un servicio verdaderamente profesional.",
							5)
			};

			for (Testimonial testimonial : testimonials) {
				testimonialRepository.save(testimonial);
			}

			logger.info("Testimonios inicializados correctamente: {} registros creados", testimonials.length);
		} else {
			logger.info("Los testimonios ya están inicializados");
		}
	}

	private Testimonial createTestimonial(AppSettings appSettings, String name, String role, String content,
			int rating) {
		Testimonial testimonial = new Testimonial();
		testimonial.setAppSettings(appSettings);
		testimonial.setName(name);
		testimonial.setRole(role);
		testimonial.setContent(content);
		testimonial.setRating(rating);
		testimonial.setIsActive(true);
		testimonial.setCreatedAt(LocalDateTime.now());
		testimonial.setUpdatedAt(LocalDateTime.now());
		return testimonial;
	}

	// Métodos para construir las plantillas HTML de los emails (sin cambios)
	private String buildWelcomeEmailTemplate() {
		return """
				    <!DOCTYPE html>
				<html>
				<head>
				    <meta charset="utf-8">
				    <title>Bienvenido a JCE Consulta</title>
				    <style>
				        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4; }
				        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
				        .header { background-color: #2c3e50; color: white; padding: 30px; text-align: center; }
				        .content { padding: 30px; }
				        .footer { background-color: #ecf0f1; padding: 20px; text-align: center; font-size: 14px; color: #7f8c8d; }
				        .btn { display: inline-block; padding: 12px 24px; background-color: #3498db; color: white; text-decoration: none; border-radius: 6px; margin: 20px 0; }
				    </style>
				</head>
				<body>
				    <div class="container">
				        <div class="header">
				            <h1>¡Bienvenido a JCE Consulta!</h1>
				        </div>
				        <div class="content">
				            <h2>Hola {{userName}},</h2>
				            <p>¡Nos complace darte la bienvenida a nuestra plataforma de consultoría profesional!</p>
				            <p>En JCE Consulta encontrarás profesionales especializados listos para ayudarte con tus proyectos y consultas.</p>
				            <p>Tu cuenta ha sido creada exitosamente. Ya puedes comenzar a explorar nuestros servicios.</p>
				            <a href="{{loginUrl}}" class="btn">Acceder a mi cuenta</a>
				            <p>Si tienes alguna pregunta, no dudes en contactarnos.</p>
				            <p>¡Gracias por confiar en nosotros!</p>
				        </div>
				        <div class="footer">
				            <p>&copy; 2025 JCE Consulta. Todos los derechos reservados.</p>
				        </div>
				    </div>
				</body>
				</html>
				""";
	}

	private String buildLoginEmailTemplate() {
		return """
				    <!DOCTYPE html>
				<html>
				<head>
				    <meta charset="utf-8">
				    <title>Notificación de Acceso</title>
				    <style>
				        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4; }
				        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; }
				        .header { background-color: #27ae60; color: white; padding: 20px; text-align: center; }
				        .content { padding: 30px; }
				        .info { background-color: #ecf0f1; padding: 15px; border-radius: 4px; margin: 20px 0; }
				        .footer { background-color: #ecf0f1; padding: 15px; text-align: center; font-size: 12px; color: #7f8c8d; }
				    </style>
				</head>
				<body>
				    <div class="container">
				        <div class="header">
				            <h2>Nuevo Acceso a tu Cuenta</h2>
				        </div>
				        <div class="content">
				            <p>Hola {{userName}},</p>
				            <p>Te notificamos que se ha registrado un nuevo acceso a tu cuenta en JCE Consulta.</p>
				            <div class="info">
				                <strong>Detalles del acceso:</strong><br>
				                Fecha y hora: {{loginDateTime}}<br>
				                Dirección IP: {{ipAddress}}<br>
				                Dispositivo: {{deviceInfo}}
				            </div>
				            <p>Si fuiste tú quien accedió, puedes ignorar este mensaje.</p>
				            <p>Si no reconoces este acceso, te recomendamos cambiar tu contraseña inmediatamente.</p>
				        </div>
				        <div class="footer">
				            <p>JCE Consulta - Sistema de Notificaciones</p>
				        </div>
				    </div>
				</body>
				</html>
				""";
	}

	private String buildAdminLoginEmailTemplate() {
		return """
				    <!DOCTYPE html>
				<html>
				<head>
				    <meta charset="utf-8">
				    <title>Acceso de Administrador</title>
				    <style>
				        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4; }
				        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; }
				        .header { background-color: #e74c3c; color: white; padding: 20px; text-align: center; }
				        .content { padding: 30px; }
				        .warning { background-color: #fff5f5; border: 1px solid #fed7d7; padding: 15px; border-radius: 4px; margin: 20px 0; }
				        .footer { background-color: #ecf0f1; padding: 15px; text-align: center; font-size: 12px; color: #7f8c8d; }
				    </style>
				</head>
				<body>
				    <div class="container">
				        <div class="header">
				            <h2>Acceso de Administrador Detectado</h2>
				        </div>
				        <div class="content">
				            <p>Se ha detectado un acceso con privilegios de administrador en JCE Consulta.</p>
				            <div class="warning">
				                <strong>Información de seguridad:</strong><br>
				                Usuario: {{adminName}}<br>
				                Fecha y hora: {{loginDateTime}}<br>
				                Dirección IP: {{ipAddress}}<br>
				                Navegador: {{browserInfo}}
				            </div>
				            <p>Este es un mensaje automático de seguridad para notificar accesos administrativos al sistema.</p>
				            <p>Si este acceso no fue autorizado, contacta inmediatamente al equipo de seguridad.</p>
				        </div>
				        <div class="footer">
				            <p>JCE Consulta - Sistema de Seguridad</p>
				        </div>
				    </div>
				</body>
				</html>
				""";
	}

	private String buildPaymentConfirmedEmailTemplate() {
		return """
				    <!DOCTYPE html>
				<html>
				<head>
				    <meta charset="utf-8">
				    <title>Pago Confirmado</title>
				    <style>
				        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4; }
				        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; }
				        .header { background-color: #27ae60; color: white; padding: 20px; text-align: center; }
				        .content { padding: 30px; }
				        .payment-details { background-color: #f8f9fa; border: 1px solid #e9ecef; padding: 20px; border-radius: 4px; margin: 20px 0; }
				        .amount { font-size: 24px; font-weight: bold; color: #27ae60; text-align: center; margin: 15px 0; }
				        .footer { background-color: #ecf0f1; padding: 20px; text-align: center; font-size: 14px; color: #7f8c8d; }
				    </style>
				</head>
				<body>
				    <div class="container">
				        <div class="header">
				            <h1>Pago Confirmado</h1>
				        </div>
				        <div class="content">
				            <p>Estimado/a {{clientName}},</p>
				            <p>¡Excelentes noticias! Hemos confirmado el pago de tu consulta en JCE Consulta.</p>
				            <div class="payment-details">
				                <h3>Detalles del pago:</h3>
				                <div class="amount">${{amount}}</div>
				                <p><strong>Número de transacción:</strong> {{transactionId}}</p>
				                <p><strong>Fecha:</strong> {{paymentDate}}</p>
				                <p><strong>Método de pago:</strong> {{paymentMethod}}</p>
				                <p><strong>Servicio:</strong> {{serviceName}}</p>
				            </div>
				            <p>Tu consulta ha sido procesada y nuestro equipo se pondrá en contacto contigo a la brevedad.</p>
				            <p>Recibirás todas las actualizaciones sobre tu consulta en esta dirección de correo.</p>
				            <p>¡Gracias por confiar en nuestros servicios!</p>
				        </div>
				        <div class="footer">
				            <p>&copy; 2025 JCE Consulta. Todos los derechos reservados.</p>
				            <p>Para soporte: support@jceconsulta.com</p>
				        </div>
				    </div>
				</body>
				</html>
				""";
	}
}
