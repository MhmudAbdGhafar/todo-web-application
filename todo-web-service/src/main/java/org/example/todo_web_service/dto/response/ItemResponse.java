package org.example.todo_web_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.todo_web_service.entities.Priority;
import org.example.todo_web_service.entities.TodoStatus;

import java.time.LocalDate;

@Schema(name = "ItemResponse", description = "Returned todo item payload.")
public record ItemResponse(

        @Schema(example = "10")
        Long id,

        @Schema(example = "Finish Swagger docs")
        String title,

        @Schema(description = "Owner user id", example = "5")
        Long userId,

        @Schema(description = "Item details")
        Details details
) {
    @Schema(name = "ItemDetailsResponse", description = "Nested details of the todo item.")
    public record Details(

            @Schema(example = "Document all endpoints using springdoc-openapi")
            String description,

            @Schema(example = "2026-01-29")
            LocalDate createdAt,

            @Schema(example = "HIGH")
            Priority priority,

            @Schema(example = "TODO")
            TodoStatus status
    ) {}
}