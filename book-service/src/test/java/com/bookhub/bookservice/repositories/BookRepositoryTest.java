package com.bookhub.bookservice.repositories;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.models.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@Transactional
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    Book setUpBook(BookStatus status){
        Book book = Book.builder()
                .title("Title")
                .description("description")
                .ageLimit(0)
                .authorId(UUID.randomUUID())
                .status(status)
                .countOfStars(0)
                .build();
        return bookRepository.save(book);
    }

    @Test
    void existsByIdAndStatus() {
        Book book = setUpBook(BookStatus.DRAFT);
        assertTrue(bookRepository.existsByIdAndStatus(book.getId(), BookStatus.DRAFT));
        assertFalse(bookRepository.existsByIdAndStatus(book.getId(), BookStatus.PUBLISHED));
    }

    @Test
    void incrementStars() {
    }

    @Test
    void findAllByIdsAndStatusOrIdAndAuthorId() {
    }

    @Test
    void findAllByTitleAndAuthorIdAndStatus() {
    }
}