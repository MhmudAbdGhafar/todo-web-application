package org.example.todo_web_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.todo_web_service.client.UserServiceClient;
import org.example.todo_web_service.config.TestMvcConfig;
import org.example.todo_web_service.dto.request.CreateItemRequest;
import org.example.todo_web_service.dto.request.UpdateItemRequest;
import org.example.todo_web_service.dto.response.CheckTokenResponse;
import org.example.todo_web_service.dto.response.ItemResponse;
import org.example.todo_web_service.entities.Priority;
import org.example.todo_web_service.entities.TodoStatus;
import org.example.todo_web_service.exception.GlobalExceptionHandlers;
import org.example.todo_web_service.exception.NotFoundException;
import org.example.todo_web_service.services.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@Import({ GlobalExceptionHandlers.class, TestMvcConfig.class })
class ItemControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    ItemService itemService;

    @MockitoBean
    UserServiceClient userServiceClient;

    private static final String AUTH = "Bearer valid-token";
    private static final Long USER_ID = 1L;

    @BeforeEach
    void authSetup() {
        given(userServiceClient.checkToken(AUTH))
                .willReturn(validToken());
    }

    // ---------- CREATE ----------
    @Test
    void createItem_200() throws Exception {
        var req = new CreateItemRequest(
                "Title",
                "Desc",
                Priority.HIGH,
                TodoStatus.TODO
        );

        given(itemService.create(eq(USER_ID), any()))
                .willReturn(sampleItem());

        mvc.perform(post("/api/items")
                        .header(HttpHeaders.AUTHORIZATION, AUTH)
                        .contentType(APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void createItem_validationError_400() throws Exception {
        var req = new CreateItemRequest(
                "",          // invalid title
                "desc",
                null,        // missing priority
                TodoStatus.TODO
        );

        mvc.perform(post("/api/items")
                        .header(HttpHeaders.AUTHORIZATION, AUTH)
                        .contentType(APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.validationErrors.title").exists())
                .andExpect(jsonPath("$.details.validationErrors.priority").exists());
    }

    @Test
    void createItem_malformedJson_400() throws Exception {
        mvc.perform(post("/api/items")
                        .header(HttpHeaders.AUTHORIZATION, AUTH)
                        .contentType(APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));
    }

    // ---------- UPDATE ----------
    @Test
    void updateItem_200() throws Exception {
        var req = new UpdateItemRequest(
                "Updated",
                null,
                Priority.MEDIUM,
                TodoStatus.IN_PROGRESS
        );

        given(itemService.update(eq(USER_ID), eq(10L), any()))
                .willReturn(sampleItem());

        mvc.perform(put("/api/items/{id}", 10)
                        .header(HttpHeaders.AUTHORIZATION, AUTH)
                        .contentType(APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_notFound_404() throws Exception {
        var req = new UpdateItemRequest(
                "Updated",
                null,
                Priority.LOW,
                TodoStatus.DONE
        );

        given(itemService.update(eq(USER_ID), eq(99L), any()))
                .willThrow(new NotFoundException("Item not found"));

        mvc.perform(put("/api/items/{id}", 99)
                        .header(HttpHeaders.AUTHORIZATION, AUTH)
                        .contentType(APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Item not found"));
    }

    @Test
    void updateItem_validationError_400() throws Exception {
        var req = new UpdateItemRequest(
                "x".repeat(300), // exceeds max size
                null,
                null,
                null
        );

        mvc.perform(put("/api/items/{id}", 10)
                        .header(HttpHeaders.AUTHORIZATION, AUTH)
                        .contentType(APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }


    // ---------- DELETE ----------
    @Test
    void deleteItem_204() throws Exception {
        mvc.perform(delete("/api/items/{id}", 10)
                        .header(HttpHeaders.AUTHORIZATION, AUTH))
                .andExpect(status().isOk());
    }

    @Test
    void deleteItem_notFound_404() throws Exception {

        doThrow(new NotFoundException("Item not found"))
                .when(itemService)
                .delete(USER_ID, 10L);

        mvc.perform(delete("/api/items/{id}", 10)
                        .header(HttpHeaders.AUTHORIZATION, AUTH))
                .andExpect(status().isNotFound());
    }

    // ---------- GET BY ID ----------
    @Test
    void getItemById_200() throws Exception {
        given(itemService.getById(USER_ID, 10L))
                .willReturn(sampleItem());

        mvc.perform(get("/api/items/{id}", 10)
                        .header(HttpHeaders.AUTHORIZATION, AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getItemById_invalidId_400() throws Exception {
        mvc.perform(get("/api/items/{id}", "abc")
                        .header(HttpHeaders.AUTHORIZATION, AUTH))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid value for parameter 'id'"));
    }

    @Test
    void getItemById_notFound_404() throws Exception {
        given(itemService.getById(USER_ID, 99L))
                .willThrow(new NotFoundException("Item not found"));

        mvc.perform(get("/api/items/{id}", 99)
                        .header(HttpHeaders.AUTHORIZATION, AUTH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Item not found"));
    }


    // ---------- SEARCH ----------
    @Test
    void searchItems_200() throws Exception {
        given(itemService.searchByTitle(eq(USER_ID), eq("test")))
                .willReturn(List.of(sampleItem()));

        mvc.perform(get("/api/items")
                        .param("title", "test")
                        .header(HttpHeaders.AUTHORIZATION, AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Title"));
    }

    @Test
    void searchItems_missingTitleParam_400() throws Exception {
        mvc.perform(get("/api/items")
                        .header(HttpHeaders.AUTHORIZATION, AUTH))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void searchItems_unexpectedError_500() throws Exception {
        given(itemService.searchByTitle(anyLong(), anyString()))
                .willThrow(new RuntimeException("DB down"));

        mvc.perform(get("/api/items")
                        .param("title", "test")
                        .header(HttpHeaders.AUTHORIZATION, AUTH))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected error occurred"));
    }

    // ---------- AUTH FAILURES ----------
    @Test
    void missingAuthHeader_401() throws Exception {
        mvc.perform(get("/api/items"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidToken_401() throws Exception {
        given(userServiceClient.checkToken(AUTH))
                .willReturn(new CheckTokenResponse(false, null, null, null, "Invalid"));

        mvc.perform(get("/api/items")
                        .header(HttpHeaders.AUTHORIZATION, AUTH))
                .andExpect(status().isUnauthorized());
    }

    // ---------- helpers ----------
    private static CheckTokenResponse validToken() {
        return new CheckTokenResponse(
                true,
                USER_ID,
                "user@example.com",
                "2026-01-27T22:56:42Z",
                "OK"
        );
    }

    private static ItemResponse sampleItem() {
        return new ItemResponse(
                10L,
                "Title",
                USER_ID,
                new ItemResponse.Details(
                        "Desc",
                        LocalDate.now(),
                        Priority.HIGH,
                        TodoStatus.TODO
                )
        );
    }
}