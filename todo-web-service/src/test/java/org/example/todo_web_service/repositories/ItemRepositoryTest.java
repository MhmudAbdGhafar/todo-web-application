package org.example.todo_web_service.repositories;

import org.example.todo_web_service.entities.Item;
import org.example.todo_web_service.entities.ItemDetails;
import org.example.todo_web_service.entities.Priority;
import org.example.todo_web_service.entities.TodoStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemRepositoryTest {

    @Autowired
    private ItemRepository repo;

    @Test
    void findByIdAndUserId_works() {

        ItemDetails details = ItemDetails.builder()
                .description("d")
                .createdAt(LocalDate.now())
                .priority(Priority.MEDIUM)
                .status(TodoStatus.DONE)
                .build();

        Item item = Item.builder()
                .title("Test")
                .userId(10L)
                .build();

        item.setDetails(details);

        Item saved = repo.save(item);

        assertThat(repo.findByIdAndUserId(saved.getId(), 10L)).isPresent();
        assertThat(repo.findByIdAndUserId(saved.getId(), 99L)).isEmpty();
    }
}