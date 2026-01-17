package org.example.todo_web_service.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @OneToOne(
            mappedBy = "item",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private ItemDetails details;

    public void setDetails(ItemDetails details) {
        this.details = details;
        if (details != null) {
            details.setItem(this);
        }
    }
}