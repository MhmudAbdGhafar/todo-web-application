package org.example.user_web_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.example.user_web_service.validation.Otp6Digits;

@Schema(
        name = "ActivateRequest",
        description = "Request body used to activate a newly registered account using a 6-digit OTP."
)
public record ActivateRequest(

        @Schema(
                description = "6-digit one-time password sent to the user's email.",
                example = "123456",
                pattern = "\\d{6}",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Otp6Digits
        String otp
) {}