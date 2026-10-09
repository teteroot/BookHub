package com.bookhub.bookservice.orchestrators.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.BookAlreadyRequireStatusException;
import com.bookhub.bookservice.exceptions.extensions.BookContentNotFoundException;
import com.bookhub.bookservice.exceptions.extensions.BookNotDraftingException;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.orchestrators.BookLifecycleOrchestrator;
import com.bookhub.bookservice.orchestrators.BookMediaOrchestrator;
import com.bookhub.bookservice.ports.ProfileProvisioningPort;
import com.bookhub.bookservice.services.BookManagementService;
import com.bookhub.bookservice.services.PageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Slf4j
@RequiredArgsConstructor
@Service
public class BookLifecycleOrchestratorImpl implements BookLifecycleOrchestrator {

    private final BookManagementService bookManagementService;
    private final PageService pageService;
    private final ProfileProvisioningPort profileProvisioningPort;
    private final BookMediaOrchestrator bookMediaOrchestrator;

    @Override
    public UUID createBook(Book book, UUID authorId) {
        book.setAuthorId(authorId);
        book.setStatus(BookStatus.EMPTY);
        bookManagementService.createBook(book);
        return book.getId();
    }

    @Override
    public void publishBook(UUID bookId, UUID authorId) {
        var book = bookManagementService.loadAuthorBookByUUID(bookId, authorId);
        if (!book.getStatus().equals(BookStatus.DRAFT)) {
            throw new BookNotDraftingException(book.getStatus());
        }
        if (pageService.getCountOfPages(book.getId()) == 0){
            throw new BookContentNotFoundException();
        }
        if (book.getS3ArchivePath() == null){
            bookMediaOrchestrator.cacheBookContent(book.getId());
        }
        if (book.getS3CoverPath() == null) {
            bookMediaOrchestrator.cacheBookCoverFromFirstPage(book.getId());
        }
        bookManagementService.updateBookStatus(book.getId(), BookStatus.PUBLISHED);
    }

    @Override
    public void draftBook(UUID bookId, UUID authorId) {
        var book = bookManagementService.loadAuthorBookByUUID(bookId, authorId);
        if (book.getStatus().equals(BookStatus.DRAFT)) {
            throw new BookAlreadyRequireStatusException(BookStatus.DRAFT);
        }
        if (book.getStatus().equals(BookStatus.EMPTY)){
            throw new BookContentNotFoundException();
        }
        bookManagementService.updateBookStatus(book.getId(), BookStatus.DRAFT);
    }

    @Override
    public void archiveBook(UUID bookId, UUID authorId) {
        var book = bookManagementService.loadAuthorBookByUUID(bookId, authorId);
        if (book.getStatus().equals(BookStatus.ARCHIVED)) {
            throw new BookAlreadyRequireStatusException(BookStatus.ARCHIVED);
        }
        if (book.getStatus().equals(BookStatus.EMPTY)){
            throw new BookContentNotFoundException();
        }
        bookManagementService.updateBookStatus(book.getId(), BookStatus.ARCHIVED);
    }

    @Override
    public void deleteBook(UUID bookId, UUID authorId) {
        bookManagementService.loadAuthorBookByUUID(bookId,authorId);
        profileProvisioningPort.removeBookReferencesFromAllFavorites(bookId.toString(), authorId.toString());
        bookManagementService.deleteBookByUUID(bookId);
        bookMediaOrchestrator.removeBookContent(bookId);
    }
}
