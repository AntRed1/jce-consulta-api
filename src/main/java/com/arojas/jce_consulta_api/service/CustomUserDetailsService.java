/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.arojas.jce_consulta_api.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.arojas.jce_consulta_api.entity.User;
import com.arojas.jce_consulta_api.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author arojas
 *         * Servicio personalizado para cargar detalles de usuario para Spring
 *         Security
 *         * Implementa UserDetailsService y anula el método loadUserByUsername
 *         * Utiliza UserRepository para buscar usuarios por su nombre de
 *         usuario
 *         (email)
 *         * Lanza UsernameNotFoundException si el usuario no se encuentra
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		log.debug("Cargando usuario por email: {}", email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> {
					log.warn("Usuario no encontrado con email: {}", email);
					return new UsernameNotFoundException("Usuario no encontrado: " + email);
				});

		if (user.getIsActive() == null || !user.getIsActive()) {
			log.warn("Intento de acceso con usuario inactivo: {}", email);
			throw new UsernameNotFoundException("Usuario inactivo: " + email);
		}

		log.debug("Usuario cargado exitosamente: {}", email);

		return org.springframework.security.core.userdetails.User.builder()
				.username(user.getEmail())
				.password(user.getPassword())
				.authorities(Collections.singletonList(
						new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
				.accountExpired(false)
				.accountLocked(false)
				.credentialsExpired(false)
				.disabled(!user.getIsActive())
				.build();
	}
}
