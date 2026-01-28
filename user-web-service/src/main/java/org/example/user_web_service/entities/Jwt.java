package org.example.user_web_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "jwts",
        indexes = {
                @Index(name = "idx_jwt_token", columnList = "token"),
                @Index(name = "idx_jwt_user", columnList = "user_id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jwt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType tokenType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}