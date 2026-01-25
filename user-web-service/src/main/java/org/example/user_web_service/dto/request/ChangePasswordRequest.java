package org.example.user_web_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.user_web_service.validation.PasswordMatch;

@PasswordMatch
public record ChangePasswordRequest(
        @NotBlank @Pattern(regexp="\\d{6}") String otp,
        @NotBlank @Size(min=8, max=64) String password,
        @NotBlank @Size(min=8, max=64) String confirmPassword
) {}
