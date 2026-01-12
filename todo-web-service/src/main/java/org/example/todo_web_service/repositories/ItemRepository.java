package org.example.todo_web_service.repositories;

import org.example.todo_web_service.entities.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}