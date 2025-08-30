/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.health;

import org.springframework.boot.actuate.health.*;
import org.springframework.stereotype.Component;

import com.arojas.jce_consulta_api.service.JceClient;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author arojas
 *         * Indicador de salud para el servicio JCE
 *         * Implementa chequeos de salud personalizados si es necesario
 */

@Component
@RequiredArgsConstructor
public class JceServiceHealthIndicator implements HealthIndicator {

	private final JceClient jceClient;

	@Override
	public Health health() {
		try {
			// Test JCE connectivity with a test query or ping
			boolean isHealthy = jceClient.checkJceServiceHealth();

			if (isHealthy) {
				return Health.up()
						.withDetail("service", "JCE")
						.withDetail("status", "Available")
						.build();
			} else {
				return Health.down()
						.withDetail("service", "JCE")
						.withDetail("status", "Unavailable")
						.build();
			}
		} catch (Exception e) {
			return Health.down()
					.withDetail("service", "JCE")
					.withDetail("error", e.getMessage())
					.build();
		}
	}
}
