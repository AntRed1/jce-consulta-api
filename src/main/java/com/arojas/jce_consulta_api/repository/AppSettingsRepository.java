/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.arojas.jce_consulta_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.arojas.jce_consulta_api.entity.AppSettings;

/**
 *
 * @author arojas
 */
@Repository
public interface AppSettingsRepository extends JpaRepository<AppSettings, String> {

	/** Última configuración activa */
	@Query("SELECT a FROM AppSettings a WHERE a.isActive = true ORDER BY a.createdAt DESC")
	Optional<AppSettings> findActiveSettings();

	/** Número de configuraciones activas */
	@Query("SELECT COUNT(a) FROM AppSettings a WHERE a.isActive = true")
	long countActiveSettings();

	/** Primera configuración (para reemplazar el .findFirst() del servicio) */
	Optional<AppSettings> findFirstByOrderByCreatedAtAsc();
}
