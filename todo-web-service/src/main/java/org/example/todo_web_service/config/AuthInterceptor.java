package org.example.todo_web_service.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.todo_web_service.client.UserServiceClient;
import org.example.todo_web_service.dto.response.CheckTokenResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final UserServiceClient userServiceClient;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (auth == null || !auth.startsWith("Bearer ")) {

            response.sendError(401, "Missing or invalid Authorization header");

            return false;
        }

        CheckTokenResponse res = userServiceClient.checkToken(auth);

        if (res == null || !res.valid()) {

            response.sendError(401, res != null ? res.message() : "Invalid token");

            return false;
        }

        // make userId available to controllers/services
        request.setAttribute("userId", res.userId());
        request.setAttribute("email", res.email());

        return true;
    }
}