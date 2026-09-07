package com.bookhub.bookservice.repositories;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.models.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@Transactional
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;
    @PersistenceContext
    private EntityManager em;

    Book setUpBook(BookStatus status, UUID authorId){
        Book book = Book.builder()
                .title("Title")
                .description("description")
                .ageLimit(0)
                .authorId(authorId)
                .status(status)
                .countOfStars(0)
                .build();
        return bookRepository.saveAndFlush(book);
    }

    @Test
    void testExistsByIdAndStatus() {
        Book book = setUpBook(BookStatus.DRAFT, UUID.randomUUID());
        assertTrue(bookRepository.existsByIdAndStatus(book.getId(), BookStatus.DRAFT));
        assertFalse(bookRepository.existsByIdAndStatus(book.getId(), BookStatus.PUBLISHED));
    }

    @Test
    void testIncrementStars() {
        Book book = setUpBook(BookStatus.DRAFT, UUID.randomUUID());
        var count = 10;
        assertEquals(1, bookRepository.incrementStars(book.getId(), count));
        em.clear();
        assertEquals(count + book.getCountOfStars(), bookRepository.findById(book.getId()).get().getCountOfStars());
    }

    @Test
    void testFindAllByIdsAndStatusOrIdAndAuthorId() {
        Book book1 = setUpBook(BookStatus.DRAFT, UUID.randomUUID());
        Book book2 = setUpBook(BookStatus.DRAFT, UUID.randomUUID());
        Book book3 = setUpBook(BookStatus.PUBLISHED, UUID.randomUUID());
        bookRepository.saveAllAndFlush(List.of(book1, book2, book3));

        assertIterableEquals(List.of(book1,book2),
                bookRepository.findAllByIdsAndStatusOrIdAndAuthorId(
                        List.of(book1.getId(),book2.getId(),book3.getId()),
                        BookStatus.DRAFT,null)
        );

        assertIterableEquals(List.of(book3),
                bookRepository.findAllByIdsAndStatusOrIdAndAuthorId(
                        List.of(book1.getId(),book2.getId(),book3.getId()),
                        BookStatus.ARCHIVED,book3.getAuthorId())
        );

    }

    @Test
    void testFindAllByTitleAndAuthorIdAndStatus() {
        Book book1 = setUpBook(BookStatus.DRAFT, UUID.randomUUID());
        book1.setTitle("testTitle1");
        var authorId = UUID.randomUUID();
        Book book2 = setUpBook(BookStatus.DRAFT, authorId);
        book2.setTitle("testTitle2");
        bookRepository.saveAndFlush(book2);
        Book book3 = setUpBook(BookStatus.PUBLISHED, authorId);
        book3.setTitle("testTitle3");
        bookRepository.saveAndFlush(book3);
        assertIterableEquals(List.of(book1,book2,book3),
                bookRepository.findAllByTitleAndAuthorIdAndStatus(
                                "test",
                                null,
                                null,
                                PageRequest.of(0,10))
                        .getContent()
        );

        assertIterableEquals(List.of(book1,book2),
                bookRepository.findAllByTitleAndAuthorIdAndStatus(
                                "test",
                                null,
                                BookStatus.DRAFT,
                                PageRequest.of(0,10))
                        .getContent()
        );

        assertIterableEquals(List.of(book2,book3),
                bookRepository.findAllByTitleAndAuthorIdAndStatus(
                                "test",
                                book2.getAuthorId(),
                                null,
                                PageRequest.of(0,10))
                        .getContent()
        );

        assertIterableEquals(List.of(book3),
                bookRepository.findAllByTitleAndAuthorIdAndStatus(
                                "test",
                                book2.getAuthorId(),
                                BookStatus.PUBLISHED,
                                PageRequest.of(0,10))
                        .getContent()
        );

    }

}