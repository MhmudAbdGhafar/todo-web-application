package org.example.user_web_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "CheckTokenResponse",
        description = """
                Response returned after validating a JWT token.

                This endpoint is mainly used for:
                - Inter-service communication (Todo Service → User Service)
                - Token validation and user identity resolution

                If `valid = false`, other fields may be null.
                """
)
public record CheckTokenResponse(

        @Schema(
                description = "Indicates whether the provided JWT token is valid and trusted.",
                example = "true"
        )
        boolean valid,

        @Schema(
                description = "Authenticated user ID. Returned only if the token is recognized.",
                example = "10",
                nullable = true
        )
        Long userId,

        @Schema(
                description = "User email (username extracted from the token).",
                example = "user@example.com",
                nullable = true
        )
        String email,

        @Schema(
                description = "Token expiration timestamp in ISO-8601 format.",
                example = "2026-01-27T22:56:42Z",
                nullable = true
        )
        String expiresAt,

        @Schema(
                description = "Human-readable explanation of the token validation result.",
                example = "Token is valid and ready to use"
        )
        String message
) {}