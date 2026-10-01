package com.bookhub.bookservice.orchestrators;

import com.bookhub.bookservice.models.Book;

import java.util.UUID;

public interface BookQueryOrchestrator {
    Book loadBookByUUID(UUID bookId, UUID authorId);
    org.springframework.data.domain.Page<Book> loadBooks(Integer page, String searchQuery, UUID authorId, UUID principalId);
    void checkBookAvailability(UUID bookId);
    void starBook(UUID bookId);
    void removeStarFromBook(UUID bookId);
}
