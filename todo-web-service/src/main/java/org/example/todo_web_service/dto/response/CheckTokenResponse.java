package org.example.todo_web_service.dto.response;

public record CheckTokenResponse(
        boolean valid,
        Long userId,
        String email,
        String expiresAt,
        String message
) {}