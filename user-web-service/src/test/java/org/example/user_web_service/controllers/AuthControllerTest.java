package org.example.user_web_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.example.user_web_service.config.SecurityConfig;
import org.example.user_web_service.dto.request.*;
import org.example.user_web_service.dto.response.CheckTokenResponse;
import org.example.user_web_service.dto.response.LoginResponse;
import org.example.user_web_service.entities.Role;
import org.example.user_web_service.exception.ApiException;
import org.example.user_web_service.exception.handlers.GlobalExceptionHandlers;
import org.example.user_web_service.exception.handlers.SecurityExceptionHandlers;
import org.example.user_web_service.services.AuthService;
import org.example.user_web_service.services.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandlers.class,
        SecurityExceptionHandlers.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private AuthService authService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private PasswordEncoder passwordEncoder;

    // ---------------- REGISTER ----------------

    @Test
    void register_success_200() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "user@test.com",
                "StrongPassword123!",
                "StrongPassword123!");

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void register_invalidEmail_400() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "invalid",
                "StrongPassword123!",
                "StrongPassword123!");

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.validationErrors.email").exists());
    }

    // ---------------- ACTIVATE ----------------

    @Test
    void activate_success_200() throws Exception {
        ActivateRequest req = new ActivateRequest("123456");

        mvc.perform(post("/api/auth/activate")
                        .param("username", "user@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(authService).activate(eq("user@test.com"), any(ActivateRequest.class));
    }

    @Test
    void activate_missingOtp_400() throws Exception {
        mvc.perform(post("/api/auth/activate")
                        .param("username", "user@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ---------------- LOGIN ----------------

    @Test
    void login_success_200() throws Exception {
        LoginRequest req = new LoginRequest("user@test.com", "StrongPassword123!");

        LoginResponse response = new LoginResponse(
                "jwt-token",
                "2026-02-01T10:00:00Z"
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    void login_missingPassword_400() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "user@test.com" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_badCredentials_401() throws Exception {

        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "user@test.com",
                          "password": "wrong"
                        }
                    """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    // ---------------- CHECK TOKEN ----------------

    @Test
    void checkToken_success_200() throws Exception {

        // 🔹 Mock JWT filter behavior
        when(jwtService.getEmail("token"))
                .thenReturn("user@example.com");

        UserDetails userDetails =
                User.withUsername("user@example.com")
                        .password("pass")
                        .roles("USER")
                        .build();

        when(userDetailsService.loadUserByUsername("user@example.com"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid("token", userDetails))
                .thenReturn(true);

        // 🔹 Mock controller service
        when(authService.checkToken("token"))
                .thenReturn(new CheckTokenResponse(
                        true,
                        1L,
                        "user@example.com",
                        "2026-01-27T22:56:42Z",
                        "Token is valid"
                ));

        mvc.perform(post("/api/auth/checkToken")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void checkToken_invalidJwt_401() throws Exception {

        // 🔹 Mock JWT filter behavior
        when(jwtService.getEmail("bad-token"))
                .thenReturn("user@example.com");

        UserDetails userDetails =
                User.withUsername("user@example.com")
                        .password("pass")
                        .roles("USER")
                        .build();

        when(userDetailsService.loadUserByUsername("user@example.com"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid("bad-token", userDetails))
                .thenReturn(false);

        // 🔹 Mock controller service
        when(authService.checkToken("bad-token"))
                .thenReturn(new CheckTokenResponse(
                        false,
                        1L,
                        "user@example.com",
                        "2026-01-27T22:56:42Z",
                        "Token is invalid"
                ));

        mvc.perform(post("/api/auth/checkToken")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    @Test
    void checkToken_missingHeader_400() throws Exception {
        mvc.perform(post("/api/auth/checkToken"))
                .andExpect(status().isBadRequest());
    }

    // ---------------- FORGET PASSWORD ----------------

    @Test
    void forgetPassword_success_200() throws Exception {
        mvc.perform(post("/api/auth/forgetPassword")
                        .param("username", "user@test.com")) 
                .andExpect(status().isOk());

        verify(authService).forgetPassword("user@test.com");
    }

    @Test
    void forgetPassword_notFound_404() throws Exception {

        doThrow(new EntityNotFoundException("User not found"))
                .when(authService).forgetPassword("user@test.com");

        mvc.perform(post("/api/auth/forgetPassword")
                        .param("username", "user@test.com"))
                .andExpect(status().isNotFound());
    }


    // ---------------- CHANGE PASSWORD ----------------

    @Test
    void changePassword_success_200() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest(
                "123456",
                "user@test.com",
                "NewStrongPassword123!",
                "NewStrongPassword123!"
        );

        mvc.perform(post("/api/auth/changePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(authService).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    void changePassword_invalidOtp_400() throws Exception {

        doThrow(new ApiException(HttpStatus.BAD_REQUEST, "Invalid OTP"))
                .when(authService).changePassword(any());

        ChangePasswordRequest req = new ChangePasswordRequest(
                "000000",
                "user@test.com",
                "NewStrongPassword123!",
                "NewStrongPassword123!"
        );

        mvc.perform(post("/api/auth/changePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid OTP"));
    }


    //---------------- ADMIN: DELETE USER ----------------

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void deleteUser_success_200() throws Exception {

        DeleteRequest req = new DeleteRequest(
                "user@test.com",
                "AdminPassword123!"
        );

        mvc.perform(delete("/api/auth/user")
                        .param("username", "user@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(authService).deleteUser(eq("user@test.com"), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_forbidden_403() throws Exception {

        DeleteRequest req = new DeleteRequest(
                "user@test.com",
                "AdminPassword123!"
        );

        mvc.perform(delete("/api/auth/user")
                        .param("username", "user@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_notFound_404() throws Exception {

        doThrow(new EntityNotFoundException("User not found"))
                .when(authService).deleteUser(eq("user@test.com"), any());

        DeleteRequest req = new DeleteRequest(
                "user@test.com",
                "AdminPassword123!"
        );

        mvc.perform(delete("/api/auth/user")
                        .param("username", "user@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // ---------------- ADMIN: UPDATE USER ----------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_success_200() throws Exception {

        UpdateUserRequest req = new UpdateUserRequest(
                Role.ROLE_USER,
                true
        );

        mvc.perform(put("/api/auth/user")
                        .param("username", "user@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(authService).updateUser(eq("user@test.com"), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUser_forbidden_403() throws Exception {

        UpdateUserRequest req = new UpdateUserRequest(Role.ROLE_USER, true);

        mvc.perform(put("/api/auth/user")
                        .param("username", "user@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_validationError_400() throws Exception {

        UpdateUserRequest req = new UpdateUserRequest(
                null,   // invalid
                null
        );

        mvc.perform(put("/api/auth/user")
                        .param("username", "user@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}