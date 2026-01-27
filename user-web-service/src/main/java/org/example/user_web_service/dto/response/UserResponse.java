package org.example.user_web_service.dto.response;

import org.example.user_web_service.entities.Role;

public record UserResponse(
        Long id,
        String email,
        Role role,
        boolean enabled
) {}