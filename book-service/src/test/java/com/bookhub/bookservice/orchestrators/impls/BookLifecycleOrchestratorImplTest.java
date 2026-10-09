package com.bookhub.bookservice.orchestrators.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.BookAlreadyRequireStatusException;
import com.bookhub.bookservice.exceptions.extensions.BookContentNotFoundException;
import com.bookhub.bookservice.exceptions.extensions.BookNotDraftingException;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.ports.ProfileProvisioningPort;
import com.bookhub.bookservice.services.BookManagementService;
import com.bookhub.bookservice.services.BookStorageService;
import com.bookhub.bookservice.services.PageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookLifecycleOrchestratorImplTest {

    @Mock
    private BookManagementService bookManagementService;
    @Mock
    private BookStorageService bookStorageService;
    @Mock
    private PageService pageService;
    @Mock
    private BookMediaOrchestratorImpl bookMediaOrchestrator;
    @Mock
    private ProfileProvisioningPort profileProvisioningPort;

    @InjectMocks
    private BookLifecycleOrchestratorImpl bookLifecycleOrchestrator;

    @Test
    void createBook_setsAuthorAndEmptyStatus() {
        var book = Book.builder().build();
        var authorId = UUID.randomUUID();

        bookLifecycleOrchestrator.createBook(book, authorId);

        assertEquals(authorId, book.getAuthorId());
        assertEquals(BookStatus.EMPTY, book.getStatus());
        verify(bookManagementService).createBook(book);
    }

    @Test
    void publishBook_requiresDraftWithPagesAndPublishes() {
        var book = book(BookStatus.DRAFT);
        book.setS3ArchivePath("archive");
        book.setS3CoverPath("cover");
        when(bookManagementService.loadAuthorBookByUUID(book.getId(), book.getAuthorId())).thenReturn(book);
        when(pageService.getCountOfPages(book.getId())).thenReturn(2);

        bookLifecycleOrchestrator.publishBook(book.getId(), book.getAuthorId());

        verify(bookManagementService).updateBookStatus(book.getId(), BookStatus.PUBLISHED);
    }

    @Test
    void publishBook_rejectsEmptyContent() {
        var book = book(BookStatus.DRAFT);
        when(bookManagementService.loadAuthorBookByUUID(book.getId(), book.getAuthorId())).thenReturn(book);
        when(pageService.getCountOfPages(book.getId())).thenReturn(0);

        assertThrows(BookContentNotFoundException.class,
                () -> bookLifecycleOrchestrator.publishBook(book.getId(), book.getAuthorId()));
        verify(bookManagementService, never()).updateBookStatus(any(), any());
    }

    @Test
    void publishBook_rejectsNonDraft() {
        var book = book(BookStatus.PUBLISHED);
        when(bookManagementService.loadAuthorBookByUUID(book.getId(), book.getAuthorId())).thenReturn(book);

        assertThrows(BookNotDraftingException.class,
                () -> bookLifecycleOrchestrator.publishBook(book.getId(), book.getAuthorId()));
        verifyNoInteractions(pageService);
    }

    @Test
    void draftBook_validatesStatusAndUpdates() {
        var published = book(BookStatus.PUBLISHED);
        when(bookManagementService.loadAuthorBookByUUID(published.getId(), published.getAuthorId())).thenReturn(published);
        bookLifecycleOrchestrator.draftBook(published.getId(), published.getAuthorId());
        verify(bookManagementService).updateBookStatus(published.getId(), BookStatus.DRAFT);

        var draft = book(BookStatus.DRAFT);
        when(bookManagementService.loadAuthorBookByUUID(draft.getId(), draft.getAuthorId())).thenReturn(draft);
        assertThrows(BookAlreadyRequireStatusException.class,
                () -> bookLifecycleOrchestrator.draftBook(draft.getId(), draft.getAuthorId()));
    }

    @Test
    void archiveBook_rejectsEmptyAndArchivesPublishedBook() {
        var published = book(BookStatus.PUBLISHED);
        when(bookManagementService.loadAuthorBookByUUID(published.getId(), published.getAuthorId())).thenReturn(published);
        bookLifecycleOrchestrator.archiveBook(published.getId(), published.getAuthorId());
        verify(bookManagementService).updateBookStatus(published.getId(), BookStatus.ARCHIVED);

        var empty = book(BookStatus.EMPTY);
        when(bookManagementService.loadAuthorBookByUUID(empty.getId(), empty.getAuthorId())).thenReturn(empty);
        assertThrows(BookContentNotFoundException.class,
                () -> bookLifecycleOrchestrator.archiveBook(empty.getId(), empty.getAuthorId()));
    }

    @Test
    void deleteBook_deletesDatabaseRecordAndStorage() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        when(bookManagementService.loadAuthorBookByUUID(bookId, authorId)).thenReturn(book(BookStatus.DRAFT));

        bookLifecycleOrchestrator.deleteBook(bookId, authorId);

        verify(bookManagementService).deleteBookByUUID(bookId);
        verify(bookMediaOrchestrator).removeBookContent(bookId);
        verify(profileProvisioningPort).removeBookReferencesFromAllFavorites(String.valueOf(bookId),String.valueOf(authorId));
    }

    private static Book book(BookStatus status) {
        return Book.builder().id(UUID.randomUUID()).authorId(UUID.randomUUID()).status(status).build();
    }
}
