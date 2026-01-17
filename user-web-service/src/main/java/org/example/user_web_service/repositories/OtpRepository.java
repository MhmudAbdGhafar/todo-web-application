package org.example.user_web_service.repositories;

import org.example.user_web_service.entities.Otp;
import org.example.user_web_service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findTopByUserOrderByExpirationTimeDesc(User user);

    @Modifying
    @Transactional
    void deleteByUser(User user);
}