package org.example.user_web_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "LoginResponse",
        description = "Returned after a successful login."
)
public record LoginResponse(

        @Schema(
                description = "JWT access token. Use it as: `Authorization: Bearer <token>`",
                example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzY5NTQyMDk5LCJleHAiOjE3Njk1NDU2OTl9..."
        )
        String token,

        @Schema(
                description = "Token expiration timestamp (ISO-8601).",
                example = "2026-01-27T22:56:42Z"
        )
        String expiresAt
) {}
