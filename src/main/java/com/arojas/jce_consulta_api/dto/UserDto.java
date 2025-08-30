package com.arojas.jce_consulta_api.dto;

import java.time.LocalDateTime;

import com.arojas.jce_consulta_api.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author arojas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String id;
    private String email;
    private String name;
    private User.Role role;
    private Integer tokens;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private Boolean isActive;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastTokenUpdate;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .tokens(user.getTokens())
                .createdAt(user.getCreatedAt())
                .isActive(user.getIsActive())
                .lastTokenUpdate(user.getLastTokenUpdate())
                .build();
    }
}