package org.example.todo_web_service.dto.response;

import org.example.todo_web_service.entities.Priority;
import org.example.todo_web_service.entities.TodoStatus;

import java.time.LocalDate;

public record ItemResponse(
        Long id,
        String title,
        Long userId,
        Details details
) {
    public record Details(
            String description,
            LocalDate createdAt,
            Priority priority,
            TodoStatus status
    ) {}
}