package com.bookhub.bookservice.orchestrators;

import com.bookhub.bookservice.models.Book;

import java.util.UUID;

public interface BookLifecycleOrchestrator {

    UUID createBook(Book book, UUID authorId);

    void publishBook(UUID bookId, UUID authorId);

    void draftBook(UUID bookId, UUID authorId);

    void archiveBook(UUID bookId, UUID authorId);

    void deleteBook(UUID bookId, UUID authorId);
}