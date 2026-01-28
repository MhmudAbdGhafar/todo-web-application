package org.example.user_web_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "otps",
        indexes = {
                @Index(name = "idx_otp_user", columnList = "user_id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 6)
    private String otp;

    @Column(name = "expiration_time", nullable = false)
    private Instant expirationTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}