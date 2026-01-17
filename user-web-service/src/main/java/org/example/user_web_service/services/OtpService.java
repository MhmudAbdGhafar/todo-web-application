package org.example.user_web_service.services;

import lombok.RequiredArgsConstructor;
import org.example.user_web_service.entities.Otp;
import org.example.user_web_service.entities.User;
import org.example.user_web_service.repositories.OtpRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.otp.expirationMinutes:10}")
    private long otpExpirationMinutes;

    public Otp generateAndStore(User user) {

        otpRepository.deleteByUser(user);

        String otp = String.format("%06d", random.nextInt(1_000_000));
        Instant exp = Instant.now().plus(otpExpirationMinutes, ChronoUnit.MINUTES);

        return otpRepository.save(
                Otp.builder()
                        .user(user)
                        .otp(otp)
                        .expirationTime(exp)
                        .build()
        );
    }

    public boolean verify(User user, String otp) {

        return otpRepository
                .findTopByUserOrderByExpirationTimeDesc(user)
                .filter(o -> o.getExpirationTime().isAfter(Instant.now()))
                .map(o -> o.getOtp().equals(otp))
                .orElse(false);
    }
}