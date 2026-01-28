package org.example.user_web_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.user_web_service.entities.Role;

@Schema(
        name = "UpdateUserRequest",
        description = """
                Admin-only request to update user administrative fields.

                Notes:
                - `email` cannot be updated (email is the username / immutable identifier).
                - All fields are optional; only provided fields will be updated.
                """
)
public record UpdateUserRequest(

        @Schema(
                description = "New role for the user.",
                example = "ROLE_USER",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        Role role,

        @Schema(
                description = "Enable/disable the user account.",
                example = "true",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        Boolean enabled
) {}
