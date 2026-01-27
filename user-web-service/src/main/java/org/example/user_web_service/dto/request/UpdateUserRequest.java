package org.example.user_web_service.dto.request;

import org.example.user_web_service.entities.Role;

public record UpdateUserRequest(
        Role role,
        Boolean enabled
) {}