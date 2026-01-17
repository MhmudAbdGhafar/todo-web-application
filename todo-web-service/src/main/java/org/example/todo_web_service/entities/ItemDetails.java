package org.example.todo_web_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "items_details")
public class ItemDetails {
    @Id
    private Long id; // same as item id

    @Lob
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    private LocalDate createdAt;

    @Enumerated(EnumType.STRING)
    private Priority  priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId  // This makes ItemsDetails use Items' ID
    @JoinColumn(name = "item_id")
    private Item item;
}