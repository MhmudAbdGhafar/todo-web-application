package org.example.todo_web_service.dto.request;

import lombok.*;
import org.example.todo_web_service.models.Priority;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateItemRequest {

    private Long id;

    private String title;

    private String description;

    private LocalDate createdAt;

    private Priority priority;

    private Boolean status;
}