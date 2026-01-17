package org.example.todo_web_service.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.todo_web_service.config.AuthInterceptor;
import org.example.todo_web_service.dto.request.CreateItemRequest;
import org.example.todo_web_service.dto.response.ItemResponse;
import org.example.todo_web_service.entities.Priority;
import org.example.todo_web_service.entities.TodoStatus;
import org.example.todo_web_service.services.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ItemService itemService;

    // IMPORTANT: mock interceptor to avoid calling user-service
    @MockBean
    AuthInterceptor authInterceptor;

    @BeforeEach
    void setup() throws Exception {

        Mockito.when(authInterceptor.preHandle(any(HttpServletRequest.class),
                        any(HttpServletResponse.class),
                        any()))
                .thenAnswer(inv -> {
                    HttpServletRequest req = inv.getArgument(0);
                    // pretend checkToken succeeded
                    req.setAttribute("userId", 3L);
                    req.setAttribute("email", "test@test.com");

                    return true;
                });
    }

    @Test
    void createItem_returns200() throws Exception {

        ItemResponse response = new ItemResponse(
                17L,
                "Gym",
                3L,
                new ItemResponse.Details(
                        "do all gym activities",
                        LocalDate.now(),
                        Priority.HIGH,
                        TodoStatus.TODO
                )
        );

        Mockito.when(itemService.create(eq(3L), any(CreateItemRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy")
                        .contentType("application/json")
                        .content("""
                                {
                                  "title": "Gym",
                                  "description": "do all gym activities",
                                  "priority": "HIGH",
                                  "status": "TODO"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(17))
                .andExpect(jsonPath("$.userId").value(3))
                .andExpect(jsonPath("$.details.priority").value("HIGH"));
    }
}