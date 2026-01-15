package org.example.todo_web_service.controllers;

import org.example.todo_web_service.dto.request.CreateItemRequest;
import org.example.todo_web_service.dto.request.UpdateItemRequest;
import org.example.todo_web_service.dto.response.ItemResponse;
import org.example.todo_web_service.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping(value = "/items")
    public ResponseEntity<List<ItemResponse>> getAllItems() {

        List<ItemResponse> responses = itemService.findAll();

        return ResponseEntity.ok().body(responses);
    }

    @GetMapping(value = "/item")
    public ResponseEntity<ItemResponse> getItemById(@RequestParam("id") Long id) {

        ItemResponse response = itemService.findById(id);

        return ResponseEntity.ok().body(response);
    }

    @PostMapping(value = "/item")
    public ResponseEntity<ItemResponse> createNewItem(
            @RequestBody CreateItemRequest request) {

        ItemResponse response = itemService.save(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping(value = "/item")
    public ResponseEntity<ItemResponse> updateItem(
            @RequestBody UpdateItemRequest request) {

        ItemResponse response = itemService.update(request);

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping(value = "/item")
    public ResponseEntity<ItemResponse> deleteItemById(@RequestParam("id") Long id) {

        ItemResponse response = itemService.delete(id);

        return ResponseEntity.ok().body(response);
    }
}