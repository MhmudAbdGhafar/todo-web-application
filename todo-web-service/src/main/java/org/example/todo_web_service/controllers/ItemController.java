package org.example.todo_web_service.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.example.todo_web_service.config.OpenApiConfig;
import org.example.todo_web_service.dto.request.CreateItemRequest;
import org.example.todo_web_service.dto.request.UpdateItemRequest;
import org.example.todo_web_service.dto.response.ItemResponse;
import org.example.todo_web_service.exception.ApiErrorResponse;
import org.example.todo_web_service.exception.ApiException;
import org.example.todo_web_service.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/items")
@Validated
@Tag(name = "Items")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    private Long userId(HttpServletRequest request) {

        Object val = request.getAttribute("userId");

        if (val == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing authenticated user");
        }

        try {
            Long id = (val instanceof Long l) ? l : Long.valueOf(val.toString());

            if (id <= 0) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid authenticated user");
            }

            return id;
        } catch (NumberFormatException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid authenticated user");
        }
    }

    @Operation(
            summary = "Create a new todo item",
            description = "Creates an item owned by the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item created",
                    content = @Content(schema = @Schema(implementation = ItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized (missing/invalid token)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ItemResponse createNewItem(
            HttpServletRequest request,
            @RequestBody @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Item creation request",
                    content = @Content(
                            schema = @Schema(implementation = CreateItemRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "title": "Finish Spring Boot tasks",
                                      "description": "Complete todo-web-service Swagger docs",
                                      "priority": "HIGH",
                                      "status": "TODO"
                                    }
                                    """)
                    )
            )
            CreateItemRequest req) {

        return itemService.create(userId(request), req);
    }

    @Operation(
            summary = "Update an existing todo item",
            description = """
                    Updates fields of an item owned by the authenticated user.
                    Fields in request body are optional; null means "no change".
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item updated",
                    content = @Content(schema = @Schema(implementation = ItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ItemResponse updateItem(
            HttpServletRequest request,
            @Parameter(in = ParameterIn.PATH, required = true, example = "10",
                    description = "Item id")
            @PathVariable @Positive(message = "id must be a positive number") Long id,
            @RequestBody @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Item update request (partial update)",
                    content = @Content(
                            schema = @Schema(implementation = UpdateItemRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "title": "Finish documentation",
                                      "status": "IN_PROGRESS"
                                    }
                                    """)
                    )
            )
            UpdateItemRequest req) {

        return itemService.update(userId(request), id, req);
    }

    @Operation(
            summary = "Delete a todo item",
            description = "Deletes an item owned by the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public void deleteItemById(
            HttpServletRequest request,
            @Parameter(in = ParameterIn.PATH, required = true, example = "10",
                    description = "Item id")
            @PathVariable @Positive(message = "id must be a positive number") Long id) {

        itemService.delete(userId(request), id);
    }

    @Operation(
            summary = "Get item by id",
            description = "Returns an item owned by the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item returned",
                    content = @Content(schema = @Schema(implementation = ItemResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ItemResponse getItemById(
            HttpServletRequest request,
            @Parameter(in = ParameterIn.PATH, required = true, example = "10",
                    description = "Item id")
            @PathVariable @Positive(message = "id must be a positive number") Long id) {

        return itemService.getById(userId(request), id);
    }

    @Operation(
            summary = "Search items by title",
            description = """
                    Returns items owned by the authenticated user where title contains the given substring (case-insensitive).
                    
                    If `title` is omitted, it returns all items.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Items list returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ItemResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public List<ItemResponse> searchItems(
            HttpServletRequest request,
            @Parameter(in = ParameterIn.QUERY, example = "spring", description = "Title search keyword")
            @Size(max = 200, message = "title must be at most 200 characters")
            @NotBlank(message = "title is required")
            @RequestParam("title") String title){

        return itemService.searchByTitle(userId(request), title);
    }
}