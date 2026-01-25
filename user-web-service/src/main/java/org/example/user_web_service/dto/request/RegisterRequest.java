package org.example.user_web_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.user_web_service.validation.PasswordMatch;

@PasswordMatch
public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min=8, max=64) String password,
        @NotBlank @Size(min=8, max=64) String confirmPassword
) {}