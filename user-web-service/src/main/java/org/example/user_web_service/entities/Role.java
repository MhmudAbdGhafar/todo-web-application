package org.example.user_web_service.entities;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User authorization role. Used by Spring Security to restrict access.")
public enum Role {
    ROLE_ADMIN,
    ROLE_USER
}