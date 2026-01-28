package org.example.user_web_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.user_web_service.validation.PasswordMatch;
import org.example.user_web_service.validation.PasswordPolicy;

@PasswordMatch
@Schema(
        name = "RegisterRequest",
        description = """
                Request body used to create a new user account.

                Notes:
                - `password` and `confirmPassword` must match.
                - Account is created disabled until OTP activation is completed.
                """
)
public record RegisterRequest(

        @Schema(
                description = "User email address (username).",
                example = "user@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Email
        String email,

        @Schema(
                description = "Password (8–64 characters).",
                example = "StrongPassword123!",
                minLength = 8,
                maxLength = 64,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @PasswordPolicy
        String password,

        @Schema(
                description = "Must match `password` exactly.",
                example = "StrongPassword123!",
                minLength = 8,
                maxLength = 64,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        String confirmPassword
) {}