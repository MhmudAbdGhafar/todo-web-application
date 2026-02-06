package org.example.user_web_service.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.user_web_service.entities.User;
import org.example.user_web_service.exception.ApiErrorResponse;
import org.example.user_web_service.exception.StorageException;
import org.example.user_web_service.services.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Upload user profile photo",
            description = """
                    Uploads or replaces the authenticated user's profile photo.

                    **Rules**
                    - Max file size: 2MB
                    - Allowed formats: JPEG, PNG
                    - Old photo (if exists) is deleted automatically

                    **Authentication**
                    - Requires a valid JWT access token
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Photo uploaded successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid file (type/size)",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected error",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @PostMapping(
            value = "/photo",

            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> uploadPhoto(
            Authentication authentication,
            @Parameter(
                    description = "Image file (JPEG or PNG, max 2MB)",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestParam("file") MultipartFile file
    ) throws StorageException {

        User principal = (User) authentication.getPrincipal();
        String userEmail = principal.getEmail();

        userService.uploadPhoto(userEmail, file);

        return ResponseEntity.ok().build();
    }
}