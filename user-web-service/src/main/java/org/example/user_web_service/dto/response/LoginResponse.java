package org.example.user_web_service.dto.response;

public record LoginResponse(String token, String expiresAt) {}