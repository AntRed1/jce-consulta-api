/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.arojas.jce_consulta_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arojas.jce_consulta_api.entity.Feature;

/**
 *
 * @author arojas
 */

@Repository
public interface FeatureRepository extends JpaRepository<Feature, String> {

	List<Feature> findByAppSettingsIdAndIsActiveTrueOrderByTitle(String appSettingsId);

	List<Feature> findByIsActiveTrueOrderByTitle();
}
