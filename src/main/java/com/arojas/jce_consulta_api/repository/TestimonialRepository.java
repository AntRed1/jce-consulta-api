/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.arojas.jce_consulta_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arojas.jce_consulta_api.entity.Testimonial;

/**
 *
 * @author arojas
 */

@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, String> {

	List<Testimonial> findByAppSettingsIdAndIsActiveTrueOrderByRatingDesc(String appSettingsId);

	List<Testimonial> findByIsActiveTrueOrderByRatingDesc();

	List<Testimonial> findByIsActiveTrueOrderByName();

}
