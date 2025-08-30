/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.arojas.jce_consulta_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arojas.jce_consulta_api.entity.Banner;

/**
 *
 * @author arojas
 */

@Repository
public interface BannerRepository extends JpaRepository<Banner, String> {

	List<Banner> findByAppSettingsIdAndIsActiveTrueOrderByPriorityDesc(String appSettingsId);

	List<Banner> findByIsActiveTrueOrderByPriorityDesc();

	List<Banner> findByIsActiveTrueOrderByPriorityAsc();

}
