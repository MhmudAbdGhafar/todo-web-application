package org.example.todo_web_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.todo_web_service.util.Priority;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@Table(name = "items_details")
public class ItemsDetails {
    @Id
    private Long id; // same as item id

    @Lob
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    private LocalDate createdAt;

    @Enumerated(EnumType.STRING)
    private Priority  priority;

    private Boolean status;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId  // This makes ItemsDetails use Items' ID
    @JoinColumn(name = "item_id")
    private Item item;
}