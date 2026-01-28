package org.example.user_web_service.entities;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = """
                Type of authentication token used by the system.

                Currently supported:
                - BEARER → Standard JWT Bearer token sent via Authorization header.

                Designed for future extensibility (e.g. REFRESH, API_KEY).
                """
)
public enum TokenType {

    @Schema(
            description = "JWT Bearer token used in Authorization header."
    )
    BEARER
}