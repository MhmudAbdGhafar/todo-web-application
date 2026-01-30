package org.example.todo_web_service.services.impl;

import org.example.todo_web_service.dto.request.CreateItemRequest;
import org.example.todo_web_service.dto.request.UpdateItemRequest;
import org.example.todo_web_service.dto.response.ItemResponse;
import org.example.todo_web_service.entities.*;
import org.example.todo_web_service.exception.NotFoundException;
import org.example.todo_web_service.repositories.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item item;

    private static final Long USER_ID = 1L;
    private static final Long ITEM_ID = 10L;

    @BeforeEach
    void setup() {
        ItemDetails details = ItemDetails.builder()
                .description("desc")
                .createdAt(LocalDate.now())
                .priority(Priority.HIGH)
                .status(TodoStatus.TODO)
                .build();

        item = Item.builder()
                .id(ITEM_ID)
                .title("Title")
                .userId(USER_ID)
                .build();

        item.setDetails(details);
    }

    // ===================== CREATE =====================

    @Test
    @DisplayName("create → success")
    void createItem_success() {
        var req = new CreateItemRequest(
                "New title",
                "New desc",
                Priority.MEDIUM,
                TodoStatus.IN_PROGRESS
        );

        given(itemRepository.save(any(Item.class)))
                .willAnswer(inv -> inv.getArgument(0));

        ItemResponse res = itemService.create(USER_ID, req);

        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo("New title");
        assertThat(res.userId()).isEqualTo(USER_ID);
        assertThat(res.details().priority()).isEqualTo(Priority.MEDIUM);
    }

    // ===================== UPDATE =====================

    @Test
    @DisplayName("update → success (partial fields)")
    void updateItem_success() {
        var req = new UpdateItemRequest(
                "Updated",
                null,
                Priority.LOW,
                null
        );

        given(itemRepository.findByIdAndUserId(ITEM_ID, USER_ID))
                .willReturn(Optional.of(item));

        given(itemRepository.save(any(Item.class)))
                .willAnswer(inv -> inv.getArgument(0));

        ItemResponse res = itemService.update(USER_ID, ITEM_ID, req);

        assertThat(res.title()).isEqualTo("Updated");
        assertThat(res.details().priority()).isEqualTo(Priority.LOW);
        assertThat(res.details().status()).isEqualTo(TodoStatus.TODO); // unchanged
    }

    @Test
    @DisplayName("update → not found")
    void updateItem_notFound() {
        given(itemRepository.findByIdAndUserId(ITEM_ID, USER_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                itemService.update(USER_ID, ITEM_ID, new UpdateItemRequest(null, null, null, null))
        )
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Item not found");
    }

    // ===================== DELETE =====================

    @Test
    @DisplayName("delete → success")
    void deleteItem_success() {
        given(itemRepository.findByIdAndUserId(ITEM_ID, USER_ID))
                .willReturn(Optional.of(item));

        willDoNothing().given(itemRepository).delete(item);

        itemService.delete(USER_ID, ITEM_ID);

        then(itemRepository).should().delete(item);
    }

    @Test
    @DisplayName("delete → not found")
    void deleteItem_notFound() {
        given(itemRepository.findByIdAndUserId(ITEM_ID, USER_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.delete(USER_ID, ITEM_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Item not found");
    }

    // ===================== GET BY ID =====================

    @Test
    @DisplayName("getById → success")
    void getById_success() {
        given(itemRepository.findByIdAndUserId(ITEM_ID, USER_ID))
                .willReturn(Optional.of(item));

        ItemResponse res = itemService.getById(USER_ID, ITEM_ID);

        assertThat(res.id()).isEqualTo(ITEM_ID);
    }

    @Test
    @DisplayName("getById → not found")
    void getById_notFound() {
        given(itemRepository.findByIdAndUserId(ITEM_ID, USER_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getById(USER_ID, ITEM_ID))
                .isInstanceOf(NotFoundException.class);
    }

    // ===================== SEARCH =====================

    @Test
    @DisplayName("searchByTitle → empty list")
    void search_empty() {
        given(itemRepository.findByUserIdAndTitleContainingIgnoreCase(USER_ID, "x"))
                .willReturn(List.of());

        List<ItemResponse> res = itemService.searchByTitle(USER_ID, "x");

        assertThat(res).isEmpty();
    }

    @Test
    @DisplayName("searchByTitle → results")
    void search_success() {
        given(itemRepository.findByUserIdAndTitleContainingIgnoreCase(USER_ID, "t"))
                .willReturn(List.of(item));

        List<ItemResponse> res = itemService.searchByTitle(USER_ID, "t");

        assertThat(res).hasSize(1);
        assertThat(res.get(0).title()).isEqualTo("Title");
    }
}
