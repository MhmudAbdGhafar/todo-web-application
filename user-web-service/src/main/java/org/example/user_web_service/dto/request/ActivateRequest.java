package org.example.user_web_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ActivateRequest(
        @NotBlank @Pattern(regexp="\\d{6}") String otp
) {}