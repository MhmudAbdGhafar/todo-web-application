package org.example.user_web_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "otps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Otp {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String otp;

    @Column(name="expiration_time", nullable=false)
    private Instant expirationTime;

    @ManyToOne(optional=false, fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;
}
