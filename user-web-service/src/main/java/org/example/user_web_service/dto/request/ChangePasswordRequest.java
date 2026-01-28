package org.example.user_web_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.user_web_service.validation.PasswordMatch;

@PasswordMatch
@Schema(
        name = "ChangePasswordRequest",
        description = """
                Request body used to reset/change a user's password using a valid OTP.

                Notes:
                - OTP must be valid and not expired.
                - `password` and `confirmPassword` must match.
                """
)
public record ChangePasswordRequest(

        @Schema(
                description = "6-digit OTP sent to the user's email.",
                example = "123456",
                pattern = "\\d{6}",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Pattern(regexp = "\\d{6}")
        String otp,

        @Schema(
                description = "User email (username). Must match the email that received the OTP.",
                example = "user@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Email
        String email,

        @Schema(
                description = "New password (8–64 characters).",
                example = "NewStrongPassword123!",
                minLength = 8,
                maxLength = 64,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Size(min = 8, max = 64)
        String password,

        @Schema(
                description = "Must match `password` exactly.",
                example = "NewStrongPassword123!",
                minLength = 8,
                maxLength = 64,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Size(min = 8, max = 64)
        String confirmPassword
) {}