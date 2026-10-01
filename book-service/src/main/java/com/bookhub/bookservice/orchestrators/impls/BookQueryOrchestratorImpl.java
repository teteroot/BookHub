package com.bookhub.bookservice.orchestrators.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.BookAccessDeniedException;
import com.bookhub.bookservice.exceptions.extensions.BookNotFoundException;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.orchestrators.BookQueryOrchestrator;
import com.bookhub.bookservice.services.BookManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookQueryOrchestratorImpl implements BookQueryOrchestrator {

    private final BookManagementService bookManagementService;

    @Override
    public Book loadBookByUUID(UUID bookId,UUID authorId) {
        var book = bookManagementService.loadBookByUUID(bookId);
        if (!book.getAuthorId().equals(authorId) && !book.getStatus().equals(BookStatus.PUBLISHED)){
            throw new BookAccessDeniedException();
        }
        return book;
    }

    @Override
    public Page<Book> loadBooks(Integer page, String searchQuery, UUID authorId, UUID principalId) {
        boolean isOwner = authorId != null && authorId.equals(principalId);
        var status = isOwner ? null : BookStatus.PUBLISHED;
        return bookManagementService.searchBook(searchQuery, authorId, status, page);
    }
    @Override
    public void checkBookAvailability(UUID bookId) {
        if (!bookManagementService.isExistAndPublishedBook(bookId)){
            throw new BookNotFoundException();
        }
    }

    @Override
    public void starBook(UUID bookId) {
        bookManagementService.incrementBookStars(bookId, 1);
    }

    @Override
    public void removeStarFromBook(UUID bookId) {
        bookManagementService.incrementBookStars(bookId,-1);
    }

}
