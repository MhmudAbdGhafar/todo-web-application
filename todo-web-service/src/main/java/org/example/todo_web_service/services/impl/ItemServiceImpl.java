package org.example.todo_web_service.services.impl;

import org.example.todo_web_service.dto.request.CreateItemRequest;
import org.example.todo_web_service.dto.request.UpdateItemRequest;
import org.example.todo_web_service.dto.response.ItemResponse;
import org.example.todo_web_service.entities.Item;
import org.example.todo_web_service.entities.ItemDetails;
import org.example.todo_web_service.entities.TodoStatus;
import org.example.todo_web_service.exception.ApiException;
import org.example.todo_web_service.exception.NotFoundException;
import org.example.todo_web_service.repositories.ItemRepository;
import org.example.todo_web_service.services.ItemService;
import org.example.todo_web_service.util.ItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public ItemResponse create(Long userId, CreateItemRequest req) {

        if (req.title() == null || req.title().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Title must not be empty");
        }

        ItemDetails details = ItemDetails.builder()
                .description(req.description())
                .createdAt(LocalDate.now())
                .priority(req.priority())
                .status(req.status())
                .build();

        Item item = Item.builder()
                .title(req.title())
                .userId(userId)
                .build();

        item.setDetails(details);

        return ItemMapper.toResponse(itemRepository.save(item));
    }

    @Override
    public ItemResponse update(Long userId, Long id, UpdateItemRequest req) {

        Item item = itemRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        ItemDetails details = item.getDetails();
        if(details == null) {
            details = new ItemDetails();
            item.setDetails(details);
        }

        if (req.title() != null && req.title().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Title cannot be blank");
        }

        if (req.status() != null &&
                item.getDetails().getStatus() == TodoStatus.DONE &&
                req.status() != TodoStatus.DONE) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Completed items cannot be modified"
            );
        }

        Optional.ofNullable(req.title()).ifPresent(item::setTitle);
        Optional.ofNullable(req.description()).ifPresent(details::setDescription);
        Optional.ofNullable(req.priority()).ifPresent(details::setPriority);
        Optional.ofNullable(req.status()).ifPresent(details::setStatus);

        return ItemMapper.toResponse(itemRepository.save(item));
    }

    @Override
    public void delete(Long userId, Long id) {

        Item item = itemRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        itemRepository.delete(item);
    }

    @Override
    public ItemResponse getById(Long userId, Long id) {

        Item item = itemRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        return ItemMapper.toResponse(item);
    }

    @Override
    public List<ItemResponse> searchByTitle(Long userId, String title){

        return itemRepository.
                findByUserIdAndTitleContainingIgnoreCase(
                        userId,
                        title == null ? "" : title
                )
                .stream().
                map(ItemMapper::toResponse)
                .toList();
    }
}