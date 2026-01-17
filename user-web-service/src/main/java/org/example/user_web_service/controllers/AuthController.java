package org.example.user_web_service.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.user_web_service.dto.request.ActivateRequest;
import org.example.user_web_service.dto.request.ChangePasswordRequest;
import org.example.user_web_service.dto.request.LoginRequest;
import org.example.user_web_service.dto.request.RegisterRequest;
import org.example.user_web_service.dto.response.CheckTokenResponse;
import org.example.user_web_service.dto.response.LoginResponse;
import org.example.user_web_service.services.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public void register(@RequestBody @Valid RegisterRequest req) {
        authService.register(req);
    }

    @PostMapping("/activate")
    public void activate(@RequestParam("username") String username,
                         @RequestBody @Valid ActivateRequest req) {
        authService.activate(username, req);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/checkToken")
    public CheckTokenResponse checkToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        String token = authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : authorization;

        return authService.checkToken(token);
    }

    @PostMapping("/regenrateOtp")
    public void regenerateOtp(@RequestParam("email") String email) {
        authService.regenerateOtp(email);
    }

    @PostMapping("/forgetPassword")
    public void forgetPassword(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        String token = authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : authorization;

        authService.forgetPassword(token);
    }

    @PostMapping("/changePassword")
    public void changePassword(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody @Valid ChangePasswordRequest req
    ) {

        String token = authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : authorization;

        authService.changePassword(token, req);
    }
}