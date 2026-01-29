package org.example.todo_web_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.*;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Todo Web Service API",
                version = "1.0.0",
                description = """
                        REST API for managing todo items.
                        
                        Authentication is performed by another service (User Service).
                        This service expects a valid token and a resolved `userId` (e.g., via gateway/JWT filter).
                        """
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local")
        },
        tags = {
                @Tag(name = "Items", description = "CRUD + search endpoints for todo items")
        }
)
@SecurityScheme(
        name = OpenApiConfig.SECURITY_SCHEME_NAME,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Send: Authorization: Bearer <token>"
)
public class OpenApiConfig {
    public static final String SECURITY_SCHEME_NAME = "bearerAuth";
}
