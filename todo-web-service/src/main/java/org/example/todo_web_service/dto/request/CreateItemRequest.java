package org.example.todo_web_service.dto.request;

import lombok.*;
import org.example.todo_web_service.models.Priority;

@Getter
@Setter
public class CreateItemRequest {

    private String title;

    private String description;

    private Priority priority;

    private Boolean status;
}