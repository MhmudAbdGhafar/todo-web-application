package org.example.todo_web_service.util;

import org.example.todo_web_service.dto.response.ItemResponse;
import org.example.todo_web_service.entities.Item;

public class ItemMapper {

    public static ItemResponse toResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getTitle(),
                item.getUserId(),
                new ItemResponse.Details(
                        item.getDetails().getDescription(),
                        item.getDetails().getCreatedAt(),
                        item.getDetails().getPriority(),
                        item.getDetails().getStatus()
                )
        );
    }
}