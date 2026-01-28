package org.example.user_web_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "user@example.com", description = "User email (username)")
        @Email @NotBlank String email,

        @Schema(example = "StrongPassword123!", description = "User password")
        @NotBlank String password
) {}