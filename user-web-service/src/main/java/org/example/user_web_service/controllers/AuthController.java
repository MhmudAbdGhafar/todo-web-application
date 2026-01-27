package org.example.user_web_service.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.user_web_service.dto.request.*;
import org.example.user_web_service.dto.response.CheckTokenResponse;
import org.example.user_web_service.dto.response.LoginResponse;
import org.example.user_web_service.services.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/regenerateOtp")
    public void regenerateOtp(@RequestParam("username") String username) {

        authService.regenerateOtp(username);
    }

    @PostMapping("/forgetPassword")
    public void forgetPassword(@RequestParam("username") String username) {

        authService.forgetPassword(username);
    }

    @PostMapping("/changePassword")
    public void changePassword(@RequestBody @Valid ChangePasswordRequest req) {

        authService.changePassword(req);
    }

    @DeleteMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@RequestBody @Valid DeleteRequest req) {

        authService.deleteUser(req);
    }

    @PutMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateUser(
            @RequestParam("username")  String username,
            @RequestBody @Valid UpdateUserRequest req) {

        authService.updateUser(username, req);
    }
}