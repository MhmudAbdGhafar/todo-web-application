package org.example.todo_web_service.util;

import org.example.todo_web_service.dto.request.CreateItemRequest;
import org.example.todo_web_service.dto.request.UpdateItemRequest;
import org.example.todo_web_service.dto.response.ItemResponse;
import org.example.todo_web_service.entities.Item;
import org.example.todo_web_service.entities.ItemsDetails;

import java.time.LocalDate;
import java.util.Optional;

public class ItemMapper {

    public static Item createItem(CreateItemRequest request) {
        if(request == null) {
            return null;
        }

        Item item = Item.builder()
                .title(request.getTitle())
                .build();

        ItemsDetails itemsDetails = ItemsDetails.builder()
                .description(request.getDescription())
                .createdAt(LocalDate.now())
                .priority(request.getPriority())
                .status(request.getStatus())
                .build();

        itemsDetails.setItem(item);
        item.setItemsDetails(itemsDetails);

        return item;
    }

    public static Item updateItem(Item item, UpdateItemRequest request) {
        if(item == null || request == null) {
            return item;
        }

        Optional.ofNullable(request.getTitle()).ifPresent(item::setTitle);

        ItemsDetails details = item.getItemsDetails();
        if(details == null) {
            return item;
        }

        Optional.ofNullable(request.getCreatedAt()).ifPresent(details::setCreatedAt);

        Optional.ofNullable(request.getDescription()).ifPresent(details::setDescription);

        Optional.ofNullable(request.getPriority()).ifPresent(details::setPriority);

        Optional.ofNullable(request.getStatus()).ifPresent(details::setStatus);

        return item;
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