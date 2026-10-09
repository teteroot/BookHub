package com.bookhub.bookservice.orchestrators.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.BookAccessDeniedException;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.services.BookManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookQueryOrchestratorImplTest {

    @Mock
    private BookManagementService bookManagementService;

    @InjectMocks
    private BookQueryOrchestratorImpl bookQueryOrchestrator;

    @Test
    void loadBookByUUID_allowsAuthorAndPublishedReader() {
        var draft = book(BookStatus.DRAFT);
        when(bookManagementService.loadBookByUUID(draft.getId())).thenReturn(draft);
        assertSame(draft, bookQueryOrchestrator.loadBookByUUID(draft.getId(), draft.getAuthorId()));

        var published = book(BookStatus.PUBLISHED);
        when(bookManagementService.loadBookByUUID(published.getId())).thenReturn(published);
        assertSame(published, bookQueryOrchestrator.loadBookByUUID(published.getId(), UUID.randomUUID()));
    }

    @Test
    void loadBookByUUID_deniesUnpublishedBookToNonAuthor() {
        var book = book(BookStatus.ARCHIVED);
        when(bookManagementService.loadBookByUUID(book.getId())).thenReturn(book);

        assertThrows(BookAccessDeniedException.class,
                () -> bookQueryOrchestrator.loadBookByUUID(book.getId(), UUID.randomUUID()));
    }


    private static Book book(BookStatus status) {
        return Book.builder().id(UUID.randomUUID()).authorId(UUID.randomUUID()).status(status).build();
    }
}
