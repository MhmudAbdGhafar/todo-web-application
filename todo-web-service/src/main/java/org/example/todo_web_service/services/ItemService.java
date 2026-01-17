package org.example.todo_web_service.services;

import org.example.todo_web_service.dto.request.CreateItemRequest;
import org.example.todo_web_service.dto.request.UpdateItemRequest;
import org.example.todo_web_service.dto.response.ItemResponse;

import java.util.List;

public interface ItemService {
    ItemResponse create(Long userId, CreateItemRequest req);
    ItemResponse update(Long userId, Long id, UpdateItemRequest req);
    void delete(Long userId, Long id);
    ItemResponse getById(Long userId, Long id);
    List<ItemResponse> searchByTitle(Long userId, String title);
}