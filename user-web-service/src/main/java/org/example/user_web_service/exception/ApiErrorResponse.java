package org.example.user_web_service.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(
        name = "ApiErrorResponse",
        description = """
                Standard error response returned by the API when a request fails.

                This structure is used for:
                - Validation errors
                - Authentication / authorization failures
                - Business rule violations
                - Unexpected server errors

                Stack traces are never exposed to clients.
                """
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(

        @Schema(
                description = "Timestamp when the error occurred.",
                example = "2026-01-27T21:55:42.437Z"
        )
        Instant timestamp,

        @Schema(
                description = "HTTP status code.",
                example = "401"
        )
        int status,

        @Schema(
                description = "HTTP status reason phrase.",
                example = "Unauthorized"
        )
        String error,

        @Schema(
                description = "Readable explanation of what went wrong.",
                example = "Invalid or expired token"
        )
        String message,

        @Schema(
                description = "Request path that caused the error.",
                example = "/api/auth/login"
        )
        String path,

        @Schema(
                description = """
                        Optional additional details.

                        Examples:
                        - field validation errors
                        - constraint violations
                        - rejected values
                        """,
                nullable = true
        )
        Map<String, Object> details
) {

    public static ApiErrorResponse of(
            int status,
            String error,
            String message,
            String path
    ) {
        return new ApiErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                path,
                null
        );
    }

    public static ApiErrorResponse of(
            int status,
            String error,
            String message,
            String path,
            Map<String, Object> details
    ) {
        return new ApiErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                path,
                details
        );
    }
}