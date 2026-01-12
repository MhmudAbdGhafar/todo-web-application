package org.example.todo_web_service.util;

import org.example.todo_web_service.dto.request.CreateItemRequest;
import org.example.todo_web_service.dto.request.UpdateItemRequest;
import org.example.todo_web_service.dto.response.ItemResponse;
import org.example.todo_web_service.entities.Item;
import org.example.todo_web_service.entities.ItemsDetails;

public class ItemMapper {

    public static Item createItem(CreateItemRequest createItemRequest) {
        if(createItemRequest == null) {
            return null;
        }

        ItemsDetails itemsDetails = ItemsDetails.builder()
                .description(createItemRequest.getDescription())
                .createdAt(createItemRequest.getCreatedAt())
                .priority(createItemRequest.getPriority())
                .status(createItemRequest.getStatus())
                .build();

        return Item.builder()
                .title(createItemRequest.getTitle())
                .itemsDetails(itemsDetails)
                .build();
    }

    public static Item updateItem(UpdateItemRequest updateItemRequest) {
        if(updateItemRequest == null) {
            return null;
        }

        ItemsDetails itemsDetails = ItemsDetails.builder()
                .id(updateItemRequest.getId())
                .description(updateItemRequest.getDescription())
                .createdAt(updateItemRequest.getCreatedAt())
                .priority(updateItemRequest.getPriority())
                .status(updateItemRequest.getStatus())
                .build();

        return Item.builder()
                .id(updateItemRequest.getId())
                .title(updateItemRequest.getTitle())
                .itemsDetails(itemsDetails)
                .build();
    }

    public static ItemResponse toItemResponse(Item item) {
        if(item == null) {
            return null;
        }

        return ItemResponse.builder()
                .title(item.getTitle())
                .description(item.getItemsDetails().getDescription())
                .createdAt(item.getItemsDetails().getCreatedAt())
                .priority(item.getItemsDetails().getPriority())
                .status(item.getItemsDetails().getStatus())
                .build();
    }
}