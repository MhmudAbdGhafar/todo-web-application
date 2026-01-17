package org.example.todo_web_service.exception;

import java.time.LocalDate;

public record ApiError(
        LocalDate timestamp,
        int status,
        String error,
        String message,
        String path
) { }