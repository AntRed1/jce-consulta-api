/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author arojas
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasksService {

	private final UserService userService;
	private final PaymentService paymentService;
	private final JceClient jceClient;

	/**
	 * Limpia tokens expirados cada hora
	 */
	@Scheduled(fixedRate = 3600000) // Cada hora
	public void cleanupExpiredTokens() {
		try {
			log.info("Iniciando limpieza programada de tokens expirados");
			userService.cleanupExpiredTokens();
		} catch (Exception e) {
			log.error("Error en limpieza programada de tokens expirados: {}", e.getMessage(), e);
		}
	}

	/**
	 * Limpia pagos pendientes expirados cada 6 horas
	 */
	@Scheduled(fixedRate = 21600000) // Cada 6 horas
	public void cleanupExpiredPayments() {
		try {
			log.info("Iniciando limpieza programada de pagos expirados");
			int cleanedPayments = paymentService.cleanupExpiredPendingPayments(24); // 24 horas
			log.info("Limpieza programada completada: {} pagos limpiados", cleanedPayments);
		} catch (Exception e) {
			log.error("Error en limpieza programada de pagos expirados: {}", e.getMessage(), e);
		}
	}

	/**
	 * Verifica la salud del servicio JCE cada 30 minutos
	 */
	@Scheduled(fixedRate = 1800000) // Cada 30 minutos
	public void checkJceServiceHealth() {
		try {
			log.debug("Verificando salud del servicio JCE");
			boolean isHealthy = jceClient.checkJceServiceHealth();
			if (!isHealthy) {
				log.warn("Servicio JCE no está respondiendo correctamente");
				// Aquí podrías enviar notificaciones a administradores
			}
		} catch (Exception e) {
			log.error("Error verificando salud del servicio JCE: {}", e.getMessage(), e);
		}
	}

	/**
	 * Genera reporte de estadísticas diario a medianoche
	 */
	@Scheduled(cron = "0 0 0 * * *") // Todos los días a medianoche
	public void generateDailyStats() {
		try {
			log.info("Generando estadísticas diarias");
			UserService.UserStatsDto userStats = userService.getUserStatistics();
			log.info("Estadísticas diarias - Usuarios totales: {}, Activos: {}, Nuevos este mes: {}",
					userStats.getTotalUsers(), userStats.getActiveUsers(), userStats.getNewUsersThisMonth());
		} catch (Exception e) {
			log.error("Error generando estadísticas diarias: {}", e.getMessage(), e);
		}
	}
}
