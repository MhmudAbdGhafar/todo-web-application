package org.example.todo_web_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.example.todo_web_service.entities.Priority;
import org.example.todo_web_service.entities.TodoStatus;

@Schema(name = "CreateItemRequest", description = "Request body to create a new todo item.")
public record CreateItemRequest(

        @Schema(description = "Item title", example = "Finish Swagger docs")
        @NotBlank(message = "title is required")
        @Size(max = 200, message = "title must be at most 200 characters")
        String title,

        @Schema(description = "Optional item description", example = "Document all endpoints using springdoc-openapi")
        @Size(max = 500, message = "description must be at most 500 characters")
        String description,

        @Schema(description = "Priority level", example = "HIGH", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "priority is required")
        Priority priority,

        @Schema(description = "Todo status", example = "TODO", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "status is required")
        TodoStatus status
) {}