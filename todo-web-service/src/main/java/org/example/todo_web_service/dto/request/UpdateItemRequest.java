package org.example.todo_web_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import org.example.todo_web_service.entities.Priority;
import org.example.todo_web_service.entities.TodoStatus;

@Schema(name = "UpdateItemRequest", description = "Request body to update an existing todo item (partial update).")
public record UpdateItemRequest(

        @Schema(description = "New title (optional)", example = "Finish documentation")
        @Size(max = 200, message = "title must be at most 200 characters")
        String title,

        @Schema(description = "New description (optional)", example = "Update swagger + global exception handler")
        @Size(max = 500, message = "description must be at most 500 characters")
        String description,

        @Schema(description = "New priority (optional)", example = "MEDIUM")
        Priority priority,

        @Schema(description = "New status (optional)", example = "IN_PROGRESS")
        TodoStatus status
) {}