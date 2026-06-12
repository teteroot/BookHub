package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.BookAlreadyExistException;
import com.bookhub.bookservice.exceptions.extensions.BookNotFoundException;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.repositories.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookManagementServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookManagementServiceImpl bookManagementService;

    @Test
    void testLoadNonExistBookByUUID() {
        UUID id = UUID.randomUUID();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> bookManagementService.loadBookByUUID(id));
    }

    @Test
    void testSuccessfulLoadBookByUUID() {
        var book = Book.builder().id(UUID.randomUUID()).build();
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        assertEquals(book, bookManagementService.loadBookByUUID(book.getId()));
    }

    @Test
    void testSuccessfulCreateBook() {
        var book = new Book();
        var authorId = UUID.randomUUID();

        bookManagementService.createBook(book, authorId);

        assertEquals(BookStatus.DRAFT, book.getStatus());
        assertEquals(authorId, book.getAuthorId());
        verify(bookRepository).saveAndFlush(book);
    }

    @Test
    void testCreateAlreadyExistBook() {
        var book = Book.builder().title("title").build();
        when(bookRepository.saveAndFlush(any(Book.class))).thenThrow(DataIntegrityViolationException.class);

        var ex = assertThrows(BookAlreadyExistException.class,
                () -> bookManagementService.createBook(book, UUID.randomUUID()));

        assertTrue(ex.getMessage().contains("title"));
    }
}