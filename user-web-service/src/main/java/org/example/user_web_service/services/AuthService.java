package org.example.user_web_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.user_web_service.dto.request.ActivateRequest;
import org.example.user_web_service.dto.request.ChangePasswordRequest;
import org.example.user_web_service.dto.request.LoginRequest;
import org.example.user_web_service.dto.request.RegisterRequest;
import org.example.user_web_service.dto.response.CheckTokenResponse;
import org.example.user_web_service.dto.response.LoginResponse;
import org.example.user_web_service.entities.Jwt;
import org.example.user_web_service.entities.User;
import org.example.user_web_service.repositories.JwtRepository;
import org.example.user_web_service.repositories.OtpRepository;
import org.example.user_web_service.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtRepository jwtRepo;
    private final OtpRepository otpRepository;
    private final OtpService otpService;
    private final MailService mailService;
    private final JwtService jwtService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public void register(RegisterRequest req) {

        if (userRepository.existsByEmail(req.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = userRepository.save(
                User.builder()
                        .email(req.email())
                        .password(encoder.encode(req.password()))
                        .enabled(false)
                        .build()
        );

        var otp = otpService.generateAndStore(user);

        try {
            mailService.sendOtp(user.getEmail(), otp.getOtp());
        }catch (Exception ex) {
            log.error("Failed to send OTP email", ex);
            // DO NOT throw — user is already saved
        }
    }

    public void activate(String usernameEmail, ActivateRequest req) {

        User user = userRepository.findByEmail(usernameEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!otpService.verify(user, req.otp())) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        user.setEnabled(true);
        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest req) {

        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.isEnabled()) throw new RuntimeException("Account not activated");

        if (!encoder.matches(req.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail());
        Instant exp = jwtService.getExpiration(token);

        jwtRepo.save(Jwt.builder()
                .token(token)
                .user(user)
                .createdAt(Instant.now())
                .expirationDate(exp)
                .tokenType("BEARER")
                .build());

        return new LoginResponse(token, exp.toString());
    }

    public CheckTokenResponse checkToken(String rawToken) {

        String email = jwtService.getEmail(rawToken); // validates signature too
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User does not exist"));

        // optional but good: ensure token exists in DB
        jwtRepo.findByToken(rawToken).orElseThrow(() -> new RuntimeException("Token not recognized"));

        return new CheckTokenResponse(
                true,
                user.getId(),
                user.getEmail(),
                jwtService.getExpiration(rawToken).toString(),
                null);
    }

    public void regenerateOtp(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var otp = otpService.generateAndStore(user);

        try {
            mailService.sendOtp(user.getEmail(), otp.getOtp());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    public void forgetPassword(String rawToken) {

        String email = jwtService.getEmail(rawToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var otp = otpService.generateAndStore(user);

        try {
            mailService.sendOtp(user.getEmail(), otp.getOtp());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    public void changePassword(String rawToken, ChangePasswordRequest req) {

        String email = jwtService.getEmail(rawToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean ok = otpService.verify(user, req.otp());
        if (!ok) throw new RuntimeException("Invalid or expired OTP");

        user.setPassword(encoder.encode(req.newPassword()));
        userRepository.save(user);

        // cleanup OTP after successful change
        otpRepository.deleteByUser(user);

        // optional but recommended: invalidate all existing tokens for the user
        jwtRepo.deleteByUser(user);
    }
}