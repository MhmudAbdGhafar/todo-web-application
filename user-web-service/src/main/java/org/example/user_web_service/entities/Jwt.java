package org.example.user_web_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "jwts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jwt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=1000)
    private String token;

    @ManyToOne(optional=false, fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    @Column(name="expiration_date", nullable=false)
    private Instant expirationDate;

    @Column(name="token_type", nullable=false)
    private String tokenType; // "BEARER"
}