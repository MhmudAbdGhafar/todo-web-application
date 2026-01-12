package org.example.todo_web_service.dto.response;

import lombok.*;
import org.example.todo_web_service.models.Priority;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponse {

    private String title;

    private String description;

    private LocalDate createdAt;

    private Priority priority;

    private Boolean status;
}