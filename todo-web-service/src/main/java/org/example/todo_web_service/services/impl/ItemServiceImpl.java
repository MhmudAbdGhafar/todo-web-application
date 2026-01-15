package org.example.todo_web_service.services.impl;

import org.example.todo_web_service.dto.request.CreateItemRequest;
import org.example.todo_web_service.dto.request.UpdateItemRequest;
import org.example.todo_web_service.dto.response.ItemResponse;
import org.example.todo_web_service.entities.Item;
import org.example.todo_web_service.repositories.ItemRepository;
import org.example.todo_web_service.services.ItemService;
import org.example.todo_web_service.util.ItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public List<ItemResponse> findAll() {
        return itemRepository.findAll()
                .stream()
                .map(ItemMapper::toItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ItemResponse findById(long id) {
        Item item = itemRepository.findById(id).orElseThrow();

        return ItemMapper.toItemResponse(item);
    }

    @Override
    public ItemResponse save(CreateItemRequest createItemRequest) {

        Item createdItem = ItemMapper.createItem(createItemRequest);

        Item savedItem = itemRepository.save(createdItem);

        return ItemMapper.toItemResponse(savedItem);
    }

    @Override
    public ItemResponse update(UpdateItemRequest request) {

        Item existingItem = itemRepository.findById(request.getId()).orElseThrow();

        Item updatedItem = ItemMapper.updateItem(existingItem, request);

        Item savedItem = itemRepository.save(updatedItem);

        return ItemMapper.toItemResponse(savedItem);
    }

    @Override
    public ItemResponse delete(long id) {

        Item deletedItem = itemRepository.findById(id).orElseThrow();

        itemRepository.delete(deletedItem);

        return ItemMapper.toItemResponse(deletedItem);
    }

}