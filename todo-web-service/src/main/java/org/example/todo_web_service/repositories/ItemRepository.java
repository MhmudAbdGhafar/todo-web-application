package org.example.todo_web_service.repositories;

import jakarta.transaction.Transactional;
import org.example.todo_web_service.entities.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByIdAndUserId(Long id, Long userId);

    List<Item> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title);
}