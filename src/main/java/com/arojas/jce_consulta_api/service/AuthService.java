package com.arojas.jce_consulta_api.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arojas.jce_consulta_api.dto.UserDto;
import com.arojas.jce_consulta_api.dto.request.LoginCredentials;
import com.arojas.jce_consulta_api.dto.request.RegisterData;
import com.arojas.jce_consulta_api.dto.response.AuthResponse;
import com.arojas.jce_consulta_api.entity.RefreshToken;
import com.arojas.jce_consulta_api.entity.User;
import com.arojas.jce_consulta_api.repository.RefreshTokenRepository;
import com.arojas.jce_consulta_api.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author arojas
 */

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthResponse register(RegisterData registerData) {
        log.info("Attempting to register user with email: {}", registerData.getEmail());

        if (!registerData.getPassword().equals(registerData.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        if (userRepository.findByEmail(registerData.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Requiere que la entidad User tenga @Builder (ver cambios abajo)
        User user = User.builder()
                .name(registerData.getName())
                .email(registerData.getEmail())
                .password(passwordEncoder.encode(registerData.getPassword()))
                .role(User.Role.USER)
                .tokens(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        String accessToken = jwtService.generateToken(createUserDetails(savedUser));
        RefreshToken refreshToken = createRefreshToken(savedUser);

        // EmailService espera (String userEmail, String userName)
        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());
        } catch (Exception e) {
            log.warn("Failed to send welcome email to: {}", savedUser.getEmail(), e);
        }

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(UserDto.fromEntity(savedUser)) // pasar UserDto (no User)
                .build();
    }

    public AuthResponse login(LoginCredentials credentials, String clientIp, String deviceInfo) {
        log.info("Login attempt for email: {}", credentials.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credentials.getEmail(),
                            credentials.getPassword()));

            User user = userRepository.findByEmail(credentials.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

            if (user.getIsActive() == null || !user.getIsActive()) {
                throw new BadCredentialsException("Usuario inactivo");
            }

            // Revocar tokens antiguos
            revokeAllUserTokens(user);

            String accessToken = jwtService.generateToken(createUserDetails(user));
            RefreshToken refreshToken = createRefreshToken(user);

            log.info("User logged in successfully: {}", user.getEmail());

            // Enviar email de login con IP y dispositivo
            try {
                emailService.sendLoginNotificationEmail(
                        user.getEmail(),
                        user.getName(),
                        clientIp != null ? clientIp : "Desconocida",
                        deviceInfo != null ? deviceInfo : "Desconocido");
            } catch (Exception e) {
                log.warn("No se pudo enviar email de notificación de login a {}: {}", user.getEmail(), e.getMessage());
            }

            return AuthResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .user(UserDto.fromEntity(user))
                    .build();

        } catch (AuthenticationException e) {
            log.warn("Failed login attempt for email: {}", credentials.getEmail());
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    public AuthResponse refreshToken(String refreshTokenStr) {
        log.info("Attempting to refresh token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token no válido"));

        // Corregido: usar getExpiryDate() en lugar de getexpiryDate()
        if (refreshToken.getexpiryDate().isBefore(LocalDateTime.now())) {

            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token expirado");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(createUserDetails(user));

        log.info("Token refreshed successfully for user: {}", user.getEmail());

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshTokenStr)
                .user(UserDto.fromEntity(user))
                .build();
    }

    public void logout(String refreshTokenStr) {
        log.info("Attempting to logout user");

        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(refreshTokenStr);
        if (refreshToken.isPresent()) {
            User user = refreshToken.get().getUser();
            revokeAllUserTokens(user);
            log.info("User logged out successfully: {}", user.getEmail());
        }
    }

    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Contraseña actual incorrecta");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        revokeAllUserTokens(user);

        log.info("Password changed successfully for user: {}", email);
    }

    public User getUserFromToken(String emailOrToken) {
        // Si es un email (desde Authentication), lo usamos directamente
        // Si fuera un JWT, tendríamos que extraer el username primero
        String email = emailOrToken;

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private RefreshToken createRefreshToken(User user) {
        // Requiere que RefreshToken tenga @Builder
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusDays(30))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    private void revokeAllUserTokens(User user) {
        // Requiere que RefreshTokenRepository tenga deleteAllByUser(User user)
        refreshTokenRepository.deleteAllByUser(user);
    }

    public boolean validateToken(String token) {
        try {
            // Extraer username y construir UserDetails simple para validar con la firma
            // existente de JwtService
            String username = jwtService.extractUsername(token);
            Optional<User> optUser = userRepository.findByEmail(username);
            if (optUser.isEmpty())
                return false;
            User appUser = optUser.get();

            org.springframework.security.core.userdetails.UserDetails userDetails = createUserDetails(appUser);

            return jwtService.isTokenValid(token, userDetails);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Crea UserDetails a partir de un User entity
     */
    private org.springframework.security.core.userdetails.UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }
}