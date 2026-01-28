package org.example.user_web_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(
        name = "DeleteRequest",
        description = """
                Admin re-authentication confirmation used to perform a sensitive operation (delete user).
                
                Notes:
                - The request must be authenticated with an Admin JWT (Authorization header).
                - This body confirms the admin password (re-auth).
                """
)
public record DeleteRequest(

        @Schema(
                description = "Admin email (username).",
                example = "admin@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Email
        String email,

        @Schema(
                description = "Admin password used for re-authentication (8–64 characters).",
                example = "AdminPassword123!",
                minLength = 8,
                maxLength = 64,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        String password
) {}