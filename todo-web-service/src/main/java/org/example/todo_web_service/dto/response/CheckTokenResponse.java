package org.example.todo_web_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CheckTokenResponse", description = "Result of validating a JWT token.")
public record CheckTokenResponse(

        @Schema(example = "true")
        boolean valid,

        @Schema(example = "5")
        Long userId,

        @Schema(example = "user@example.com")
        String email,

        @Schema(example = "2026-01-29T22:56:42Z")
        String expiresAt,

        @Schema(example = "Token is valid and ready to use")
        String message
) {}