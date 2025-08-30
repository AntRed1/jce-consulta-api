/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.arojas.jce_consulta_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arojas.jce_consulta_api.entity.EmailTemplate;

/**
 *
 * @author arojas
 */

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, String> {

	Optional<EmailTemplate> findByTypeAndIsActiveTrue(EmailTemplate.TemplateType type);

	List<EmailTemplate> findByAppSettingsIdAndIsActiveTrueOrderByName(String appSettingsId);

	List<EmailTemplate> findByIsActiveTrueOrderByName();

}
