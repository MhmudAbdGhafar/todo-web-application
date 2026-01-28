package org.example.user_web_service.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.user_web_service.dto.request.*;
import org.example.user_web_service.dto.response.CheckTokenResponse;
import org.example.user_web_service.dto.response.LoginResponse;
import org.example.user_web_service.entities.Jwt;
import org.example.user_web_service.entities.Role;
import org.example.user_web_service.entities.TokenType;
import org.example.user_web_service.entities.User;
import org.example.user_web_service.exception.ApiException;
import org.example.user_web_service.repositories.JwtRepository;
import org.example.user_web_service.repositories.OtpRepository;
import org.example.user_web_service.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepo;
    private final JwtRepository jwtRepo;
    private final OtpRepository otpRepo;
    private final OtpService otpService;
    private final MailService mailService;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;

    public void register(RegisterRequest req) {

        String email = normalizeEmail(req.email());

        if (userRepo.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = userRepo.save(
                User.builder()
                        .email(email)
                        .password(encoder.encode(req.password()))
                        .role(Role.ROLE_USER)
                        .enabled(false)
                        .build()
        );

        sendOtpEmail(user);
    }

    public void activate(String email, ActivateRequest req) {

        var user = findUserByEmail(email);

        if (user.isEnabled()) {
            throw new ApiException(HttpStatus.CONFLICT, "Account is already activated");
        }

        if (!otpService.verify(user, req.otp())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }

        user.enable();

        userRepo.save(user);
    }

    public LoginResponse login(LoginRequest req) {

        String email = normalizeEmail(req.email());

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, req.password())
        );

        var user = findUserByEmail(email);

        if (!user.isEnabled()){
            throw new ApiException(HttpStatus.FORBIDDEN, "Account not activated");
        }

        var token = jwtService.generateToken(user.getEmail());
        var exp = jwtService.getExpiration(token);

        jwtRepo.save(Jwt.builder()
                .token(token)
                .user(user)
                .createdAt(Instant.now())
                .expirationDate(exp)
                .tokenType(TokenType.BEARER)
                .build());

        return new LoginResponse(token, exp.toString());
    }

    /**
     * IMPORTANT:
     * - This endpoint is for inter-service communication (todo-service -> user-service).
     * - It should NEVER throw, only return (valid/invalid).
     */
    public CheckTokenResponse checkToken(String rawToken) {

        if (isBlank(rawToken)) {
            return invalid("Missing token");
        }

        try {
            String email = jwtService.getEmail(rawToken);
            Instant exp = jwtService.getExpiration(rawToken);

            if (isBlank(email)) {
                return invalid("Invalid token");
            }

            if (exp.isBefore(Instant.now())) {
                return invalid("Token is expired");
            }

            User user = findUserByEmail(email);

            if (jwtRepo.findByToken(rawToken).isEmpty()) {
                return invalid("Token not recognized", user, exp);
            }

            if (!jwtService.isTokenValid(rawToken, user)) {
                return invalid("Token is invalid or expired", user, exp);
            }

            return valid(user, exp);

        } catch (io.jsonwebtoken.ExpiredJwtException ex) {

            return invalid("Token is expired");
        } catch (io.jsonwebtoken.JwtException ex) {

            return invalid("Invalid token");
        } catch (Exception ex) {

            log.error("checkToken unexpected error", ex);

            return invalid("Token validation failed");
        }
    }

    public void regenerateOtp(String email) {

        var user = findUserByEmail(email);

        sendOtpEmail(user);
    }

    public void forgetPassword(String email) {

        var user = findUserByEmail(email);

        sendOtpEmail(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest req) {

        var user = findUserByEmail(req.email());

        if (!otpService.verify(user, req.otp())){
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }

        user.changePassword(encoder.encode(req.password()));
        userRepo.save(user);

        otpRepo.deleteByUser(user);

        jwtRepo.deleteByUser(user);
    }

    @Transactional
    public void deleteUser(String email, DeleteRequest req) {

        User admin = findUserByEmail(req.email());

        if (!encoder.matches(req.password(), admin.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid admin password");
        }

        User targetUser = findUserByEmail(email);

        if (targetUser.getRole() == Role.ROLE_ADMIN) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Admin accounts cannot be deleted"
            );
        }

        otpRepo.deleteByUser(targetUser);
        jwtRepo.deleteByUser(targetUser);
        userRepo.delete(targetUser);
    }

    @Transactional
    public void updateUser(String username, UpdateUserRequest req) {

        var user = findUserByEmail(username);

        if (user.getRole() == Role.ROLE_ADMIN && Boolean.FALSE.equals(req.enabled())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin accounts cannot be disabled");
        }

        if (req.role() != null) {
            user.changeRole(req.role());
        }

        if (req.enabled() != null) {

            if (req.enabled()) {
                user.enable();
            }
            else {
                user.disable();
            }
        }

        userRepo.save(user);
    }

    private void sendOtpEmail(User user) {

        var otp = otpService.generateAndStore(user);

        try {
            mailService.sendOtp(user.getEmail(), otp.getOtp());
        } catch (Exception ex) {
            log.error("Failed to send OTP email to {}", user.getEmail(), ex);
        }
    }

    private User findUserByEmail(String email) {

        return userRepo.findByEmail(normalizeEmail(email)).orElseThrow(()
                -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private CheckTokenResponse valid(User user, Instant exp) {
        return new CheckTokenResponse(
                true,
                user.getId(),
                user.getEmail(),
                exp.toString(),
                "Token is valid and ready to use"
        );
    }

    private CheckTokenResponse invalid(String message) {
        return new CheckTokenResponse(false, null, null, null, message);
    }

    private CheckTokenResponse invalid(String message, User user, Instant exp) {
        return new CheckTokenResponse(
                false,
                user.getId(),
                user.getEmail(),
                exp.toString(),
                message
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}