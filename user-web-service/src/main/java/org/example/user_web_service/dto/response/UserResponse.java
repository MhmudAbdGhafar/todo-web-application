package org.example.user_web_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.user_web_service.entities.Role;

@Schema(
        name = "UserResponse",
        description = "Represents user information returned by the API."
)
public record UserResponse(

        @Schema(description = "User id.", example = "10")
        Long id,

        @Schema(description = "User email (username).", example = "user@example.com")
        String email,

        @Schema(description = "User role.", example = "ROLE_USER")
        Role role,

        @Schema(description = "Whether the account is enabled/activated.", example = "true")
        boolean enabled
) {}