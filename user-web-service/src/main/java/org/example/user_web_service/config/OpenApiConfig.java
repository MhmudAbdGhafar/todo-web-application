package org.example.user_web_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("User Web Service API")
                        .description("""
                                Authentication and user management API for the Todo Web Application.

                                **Auth flow**
                                1) Register → OTP sent
                                2) Activate with OTP
                                3) Login → JWT token returned
                                4) Use `Authorization: Bearer <token>` for secured endpoints

                                **Inter-service**
                                - Todo service validates user tokens via `POST /api/auth/checkToken`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Todo Web Application")
                        )
                )
                .addServersItem(new Server().url("http://localhost:8081").description("Local"))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}