package org.example.todo_web_service.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ApiErrorResponse", description = "Standard error response body")
public record ApiErrorResponse(

        @Schema(example = "2026-01-29T10:15:30.123Z")
        Instant timestamp,

        @Schema(example = "400")
        int status,

        @Schema(example = "Bad Request")
        String error,

        @Schema(example = "Validation failed")
        String message,

        @Schema(example = "/api/items/10")
        String path,

        @Schema(description = "Optional extra details, e.g. validation errors")
        Map<String, Object> details
) {
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, null);
    }

    public static ApiErrorResponse of(int status, String error, String message, String path, Map<String, Object> details) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, details);
    }
}