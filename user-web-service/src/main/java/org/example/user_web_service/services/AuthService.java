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
import org.example.user_web_service.entities.Role;
import org.example.user_web_service.entities.TokenType;
import org.example.user_web_service.entities.User;
import org.example.user_web_service.repositories.JwtRepository;
import org.example.user_web_service.repositories.OtpRepository;
import org.example.user_web_service.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final UserService userService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;

    public void register(RegisterRequest req) {

        if (userRepo.existsByEmail(req.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = userRepo.save(
                User.builder()
                        .email(req.email())
                        .password(encoder.encode(req.password()))
                        .role(Role.ROLE_USER)
                        .enabled(false)
                        .build()
        );

        var otp = otpService.generateAndStore(user);

        try {
            mailService.sendOtp(user.getEmail(), otp.getOtp());
        }catch (Exception ex) {
            log.error("Failed to send OTP email", ex);
        }
    }

    public void activate(String usernameEmail, ActivateRequest req) {

        User user = userRepo.findByEmail(usernameEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!otpService.verify(user, req.otp())) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        user.setEnabled(true);

        userRepo.save(user);
    }

    public LoginResponse login(LoginRequest req) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.email(),
                        req.password()
                )
        );

        var user = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()){
            throw new RuntimeException("Account not activated");
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

    public CheckTokenResponse checkToken(String rawToken) {

        var email = jwtService.getEmail(rawToken);

        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        jwtRepo.findByToken(rawToken)
                .orElseThrow(() -> new RuntimeException("Token not recognized"));

        var userDetails = userService.loadUserByUsername(email);
        if(!jwtService.isTokenValid(rawToken, userDetails)){

            throw new RuntimeException("Token is invalid, or  expired");
        }

        return new CheckTokenResponse(
                true,
                user.getId(),
                user.getEmail(),
                jwtService.getExpiration(rawToken).toString(),
                "Token is valid and ready to use"
        );
    }

    public void regenerateOtp(String usernameEmail) {

        User user = userRepo.findByEmail(usernameEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var otp = otpService.generateAndStore(user);

        try {
            mailService.sendOtp(user.getEmail(), otp.getOtp());
        } catch (Exception ex) {
            log.error("Failed to send OTP email", ex);
        }
    }

    public void forgetPassword(String usernameEmail) {

        User user = userRepo.findByEmail(usernameEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var otp = otpService.generateAndStore(user);

        try {
            mailService.sendOtp(user.getEmail(), otp.getOtp());
        } catch (Exception ex) {
            log.error("Failed to send OTP email", ex);
        }
    }

    public void changePassword(String rawToken, ChangePasswordRequest req) {

        String email = jwtService.getEmail(rawToken);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean ok = otpService.verify(user, req.otp());
        if (!ok){
            throw new RuntimeException("Invalid or expired OTP");
        }

        user.setPassword(encoder.encode(req.password()));
        userRepo.save(user);

        otpRepo.deleteByUser(user);

        jwtRepo.deleteByUser(user);
    }
}