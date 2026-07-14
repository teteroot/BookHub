package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.*;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;
import com.bookhub.bookservice.repositories.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.ArrayList;
import java.util.List;
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
        bookManagementService.createBook(book);
        verify(bookRepository).saveAndFlush(book);
    }

    @Test
    void testCreateAlreadyExistBook() {
        var book = Book.builder().title("title").build();
        when(bookRepository.saveAndFlush(any(Book.class))).thenThrow(DataIntegrityViolationException.class);
        var ex = assertThrows(BookAlreadyExistException.class,
                () -> bookManagementService.createBook(book));
        assertTrue(ex.getMessage().contains("title"));
    }

    @Test
    void testClaimBookForUpdate_success() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var book = Book.builder()
                .id(bookId)
                .authorId(authorId)
                .status(BookStatus.EMPTY)
                .build();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        var result = bookManagementService.claimBookForUpdate(bookId, authorId);

        assertEquals(book, result);
        assertNotNull(result.getUpdatedAt());
        verify(bookRepository).saveAndFlush(book);
    }

    @Test
    void testClaimBookForUpload_bookNotFound() {
        var bookId = UUID.randomUUID();
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> bookManagementService.claimBookForUpdate(bookId, UUID.randomUUID()));
        verify(bookRepository, never()).saveAndFlush(any());
    }

    @Test
    void testClaimBookForUpdate_notOwner_throwsAccessDenied() {
        var bookId = UUID.randomUUID();
        var book = Book.builder()
                .id(bookId)
                .authorId(UUID.randomUUID())
                .status(BookStatus.EMPTY)
                .build();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        assertThrows(BookAccessDeniedException.class,
                () -> bookManagementService.claimBookForUpdate(bookId, UUID.randomUUID()));
        verify(bookRepository, never()).saveAndFlush(any());
    }

    @Test
    void testClaimBookForUpdate_statusNotEmpty_throwsContentAlreadyExist() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var book = Book.builder()
                .id(bookId)
                .authorId(authorId)
                .status(BookStatus.DRAFT)
                .build();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        assertThrows(BookContentAlreadyExistException.class,
                () -> bookManagementService.claimBookForUpdate(bookId, authorId));
        verify(bookRepository, never()).saveAndFlush(any());
    }

    @Test
    void testClaimBookForUpdate_concurrentModification_throwsCustomException() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var book = Book.builder()
                .id(bookId)
                .authorId(authorId)
                .status(BookStatus.EMPTY)
                .build();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.saveAndFlush(book)).thenThrow(ObjectOptimisticLockingFailureException.class);

        assertThrows(BookConcurrentModificationException.class,
                () -> bookManagementService.claimBookForUpdate(bookId, authorId));
    }

    @Test
    void testRemoveAllPages_clearsPagesCollection() {
        var bookId = UUID.randomUUID();
        var book = Book.builder()
                .id(bookId)
                .pages(new ArrayList<>(List.of(Page.builder().id(UUID.randomUUID()).build())))
                .build();
        when(bookRepository.getReferenceById(bookId)).thenReturn(book);

        bookManagementService.removeAllPages(bookId);

        assertTrue(book.getPages().isEmpty());
    }

    @Test
    void testRemoveAllPages_alreadyEmpty_noException() {
        var bookId = UUID.randomUUID();
        var book = Book.builder()
                .id(bookId)
                .pages(new ArrayList<>())
                .build();
        when(bookRepository.getReferenceById(bookId)).thenReturn(book);

        assertDoesNotThrow(() -> bookManagementService.removeAllPages(bookId));
        assertTrue(book.getPages().isEmpty());
    }

    @Test
    void testRemoveAllPages_bookNotFound_propagatesException() {
        var bookId = UUID.randomUUID();
        when(bookRepository.getReferenceById(bookId)).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class,
                () -> bookManagementService.removeAllPages(bookId));
    }

    @Test
    void testUpdateBookStatus_success() {
        var bookId = UUID.randomUUID();
        var book = Book.builder().id(bookId).status(BookStatus.EMPTY).build();
        when(bookRepository.getReferenceById(bookId)).thenReturn(book);

        bookManagementService.updateBookStatus(bookId, BookStatus.DRAFT);

        assertEquals(BookStatus.DRAFT, book.getStatus());
    }

    @Test
    void testUpdateBookStatus_sameStatus_noException() {
        var bookId = UUID.randomUUID();
        var book = Book.builder().id(bookId).status(BookStatus.DRAFT).build();
        when(bookRepository.getReferenceById(bookId)).thenReturn(book);

        assertDoesNotThrow(() -> bookManagementService.updateBookStatus(bookId, BookStatus.DRAFT));
        assertEquals(BookStatus.DRAFT, book.getStatus());
    }

    @Test
    void testUpdateBookStatus_bookNotFound_propagatesException() {
        var bookId = UUID.randomUUID();
        when(bookRepository.getReferenceById(bookId)).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class,
                () -> bookManagementService.updateBookStatus(bookId, BookStatus.PUBLISHED));
    }

    @Test
    void testUpdateBookContentPath_success() {
        var bookId = UUID.randomUUID();
        var book = Book.builder().id(bookId).s3ArchivePath(null).build();
        when(bookRepository.getReferenceById(bookId)).thenReturn(book);

        bookManagementService.updateBookContentPath(bookId, "new-path");

        assertEquals("new-path", book.getS3ArchivePath());
    }

    @Test
    void testUpdateBookContentPath_overwritesExistingPath() {
        var bookId = UUID.randomUUID();
        var book = Book.builder().id(bookId).s3ArchivePath("old-path").build();
        when(bookRepository.getReferenceById(bookId)).thenReturn(book);

        bookManagementService.updateBookContentPath(bookId, "new-path");

        assertEquals("new-path", book.getS3ArchivePath());
    }

    @Test
    void testUpdateBookContentPath_bookNotFound_propagatesException() {
        var bookId = UUID.randomUUID();
        when(bookRepository.getReferenceById(bookId)).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class,
                () -> bookManagementService.updateBookContentPath(bookId, "path"));
    }
}