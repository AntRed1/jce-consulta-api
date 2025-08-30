/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author arojas
 *         * Servicio para auditoría de operaciones críticas
 *
 */

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditService {

	public void logPaymentOperation(String userId, String operation, String details) {
		log.info("AUDIT_PAYMENT - User: {} | Operation: {} | Details: {}",
				userId, operation, details);

		// TODO: Persist to audit table if needed
	}

	public void logQueryOperation(String userId, String cedula, String operation, String status) {
		log.info("AUDIT_QUERY - User: {} | Cedula: {} | Operation: {} | Status: {}",
				userId, maskCedula(cedula), operation, status);

		// TODO: Persist to audit table if needed
	}

	public void logSecurityEvent(String userId, String event, String ipAddress) {
		log.warn("SECURITY_AUDIT - User: {} | Event: {} | IP: {}",
				userId, event, ipAddress);

		// TODO: Persist to security audit table
	}

	private String maskCedula(String cedula) {
		if (cedula == null || cedula.length() < 4)
			return "***";
		return cedula.substring(0, 3) + "****" + cedula.substring(cedula.length() - 2);
	}
}
