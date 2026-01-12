package org.example.todo_web_service.services;

import org.example.todo_web_service.dto.request.CreateItemRequest;
import org.example.todo_web_service.dto.request.UpdateItemRequest;
import org.example.todo_web_service.dto.response.ItemResponse;

import java.util.List;

public interface ItemService {

    List<ItemResponse> findAll();

    ItemResponse findById(long id);

    ItemResponse save(CreateItemRequest createItemRequest);

    ItemResponse update(UpdateItemRequest updateItemRequest);

    ItemResponse delete(long id);

}