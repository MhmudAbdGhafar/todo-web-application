package org.example.todo_web_service.dto.request;

import jakarta.validation.constraints.Size;

import org.example.todo_web_service.entities.Priority;
import org.example.todo_web_service.entities.TodoStatus;

public record UpdateItemRequest(
        @Size(max = 200, message = "title must be at most 200 characters")
        String title,

        @Size(max = 500, message = "description must be at most 500 characters")
        String description,

        Priority priority,

        TodoStatus status
) {}