package org.example.todo_web_service.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.todo_web_service.dto.request.CreateItemRequest;
import org.example.todo_web_service.dto.request.UpdateItemRequest;
import org.example.todo_web_service.dto.response.ItemResponse;
import org.example.todo_web_service.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/items")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    private Long userId(HttpServletRequest request) {

        Object val = request.getAttribute("userId");

        return (val instanceof Long l) ? l : Long.valueOf(val.toString());
    }

    @PostMapping
    public ItemResponse createNewItem(
            HttpServletRequest request,
            @RequestBody @Valid CreateItemRequest req) {

        return itemService.create(userId(request), req);
    }

    @PutMapping("/{id}")
    public ItemResponse updateItem(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody @Valid UpdateItemRequest req) {

        return itemService.update(userId(request), id, req);
    }

    @DeleteMapping("/{id}")
    public void deleteItemById(
            HttpServletRequest request,
            @PathVariable Long id) {

        itemService.delete(userId(request), id);
    }

    @GetMapping("/{id}")
    public ItemResponse getItemById(
            HttpServletRequest request,
            @PathVariable Long id) {

        return itemService.getById(userId(request), id);
    }

    @GetMapping
    public List<ItemResponse> searchItems(
            HttpServletRequest request,
            @RequestParam String title){

        return itemService.searchByTitle(userId(request), title);
    }
}