package org.example.user_web_service.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.example.user_web_service.config.OpenApiConfig;
import org.example.user_web_service.dto.request.*;
import org.example.user_web_service.dto.response.CheckTokenResponse;
import org.example.user_web_service.dto.response.LoginResponse;
import org.example.user_web_service.exception.ApiErrorResponse;
import org.example.user_web_service.services.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = """
                Endpoints for registration, activation (OTP), login,
                token validation, password reset, and admin user management.
                """
)
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register a new user",
            description = """
                    Creates a disabled user account and sends an OTP to the user's email.
                    **Next step:** Call `POST /api/auth/activate?username=<email>` 
                    with the OTP to enable the account.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration accepted (OTP sent)"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already exists",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/register")
    public void register(
            @RequestBody @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Registration request",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "email": "user@example.com",
                                      "password": "StrongPassword123!"
                                    }
                                    """)
                    )
            )
            RegisterRequest req) {

        authService.register(req);
    }

    @Operation(
            summary = "Activate account using OTP",
            description = """
                    Enables a user account after validating the OTP.
                    - `username` is the user's email.
                    - OTP must be valid and not expired.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account activated"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP / validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/activate")
    public void activate(
            @Parameter(
                    name = "username",
                    description = "User email used as username",
                    required = true,
                    in = ParameterIn.QUERY,
                    example = "user@example.com"
            )
            @RequestParam("username") @Email String username,


            @RequestBody @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "OTP activation request",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ActivateRequest.class),
                            examples = @ExampleObject(value = """
                                    { "otp": "123456" }
                                    """)
                    )
            )
            ActivateRequest req) {

        authService.activate(username, req);
    }

    @Operation(
            summary = "Login",
            description = """
                    Authenticates user credentials and returns a JWT access token.
                    
                    **Requirements**
                    - Account must be activated (enabled = true)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged in successfully",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "token": "eyJhbGciOiJIUzUxMiJ9....",
                                      "expiresAt": "2026-01-27T22:56:42Z"
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Account not activated",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login request",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "email": "user@example.com",
                                      "password": "StrongPassword123!"
                                    }
                                    """)
                    )
            )
            LoginRequest req) {

        return authService.login(req);
    }

    @Operation(
            summary = "Validate JWT (used by other services)",
            description = """
                    Validates a JWT token and returns whether it is usable.
                    
                    **Important**
                    - Designed for inter-service communication (e.g., Todo service calling User service).
                    - Token should be sent in `Authorization: Bearer <token>`.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token validation result",
                    content = @Content(schema = @Schema(implementation = CheckTokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Missing/invalid header or malformed request",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token invalid/expired",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/checkToken")
    public CheckTokenResponse checkToken(
            @Parameter(
                    name = HttpHeaders.AUTHORIZATION,
                    description = "Bearer access token",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "Bearer eyJhbGciOiJIUzUxMiJ9...."
            )
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        String token = authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : authorization;

        return authService.checkToken(token);
    }

    @Operation(
            summary = "Regenerate OTP",
            description = "Generates a new OTP for an existing user and sends it via email."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP regenerated (email sent)"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/regenerateOtp")
    public void regenerateOtp(
            @Parameter(
                    name = "username",
                    description = "User email used as username",
                    required = true,
                    in = ParameterIn.QUERY,
                    example = "user@example.com"
            )
            @RequestParam("username") @Email String username) {

        authService.regenerateOtp(username);
    }

    @Operation(
            summary = "Forget password (request OTP)",
            description = """
                    Sends an OTP to the user's email to reset the password.
                    
                    **Next step:** Call `POST /api/auth/changePassword` with the OTP and new password.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP generated (email sent)"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/forgetPassword")
    public void forgetPassword(
            @Parameter(
                    name = "username",
                    description = "User email used as username",
                    required = true,
                    in = ParameterIn.QUERY,
                    example = "user@example.com"
            )
            @RequestParam("username") @Email String username) {

        authService.forgetPassword(username);
    }

    @Operation(
            summary = "Change password using OTP",
            description = """
                    Updates the user's password after validating the OTP.
                    
                    After a successful password change:
                    - OTP is removed
                    - Existing tokens are revoked (if your service does this)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid/expired OTP or validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/changePassword")
    public void changePassword(
            @RequestBody @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Change password request",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ChangePasswordRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "email": "user@example.com",
                                      "otp": "123456",
                                      "password": "NewStrongPassword123!"
                                    }
                                    """)
                    )
            )
            ChangePasswordRequest req) {

        authService.changePassword(req);
    }

    @Operation(
            summary = "Delete a user (Admin only)",
            description = """
                    Deletes a user account (Admin only).
                    
                    This endpoint is protected by `hasRole('ADMIN')`.
                    """
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (missing/invalid JWT)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (not ADMIN)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(
            @Parameter(
                    name = "username",
                    description = "User email used as username (immutable)",
                    required = true,
                    in = ParameterIn.QUERY,
                    example = "user@example.com"
            )
            @RequestParam("username") @Email String username,
            @RequestBody @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Admin confirmation request",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = DeleteRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "username": "user@example.com",
                                      "password": "AdminPassword123!"
                                    }
                                    """)
                    )
            )
            DeleteRequest req) {

        authService.deleteUser(username, req);
    }

    @Operation(
            summary = "Update a user (Admin only)",
            description = """
                    Updates a user's properties (Admin only).
                    
                    - `username` is the user email (immutable identifier).
                    - Use request body to set allowed changes (e.g., role/enabled/etc.).
                    """
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (missing/invalid JWT)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (not ADMIN)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateUser(
            @Parameter(
                    name = "username",
                    description = "User email used as username (immutable)",
                    required = true,
                    in = ParameterIn.QUERY,
                    example = "user@example.com"
            )
            @RequestParam("username") @Email String username,

            @RequestBody @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User update request (admin-managed fields only)",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateUserRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "enabled": true,
                                      "role": "ROLE_USER"
                                    }
                                    """)
                    )
            )
            UpdateUserRequest req) {

        authService.updateUser(username, req);
    }
}