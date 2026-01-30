package org.example.todo_web_service.config;

import lombok.RequiredArgsConstructor;
import org.example.todo_web_service.client.UserServiceClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@TestConfiguration
@RequiredArgsConstructor
public class TestMvcConfig implements WebMvcConfigurer {

    private final UserServiceClient userServiceClient;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor());
    }

    @Bean
    AuthInterceptor authInterceptor() {
        return new AuthInterceptor(userServiceClient);
    }
}
