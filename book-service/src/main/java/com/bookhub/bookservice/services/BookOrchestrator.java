package com.bookhub.bookservice.services;

import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;
import org.springframework.http.MediaType;

import java.io.InputStream;
import java.util.UUID;

public interface BookOrchestrator {
    InputStream loadBookStream(UUID authorId, UUID bookId);
    Book loadBookByUUID(UUID uuid);
    void createBook(Book book, UUID authorId);
    void createBookContent(UUID bookId, UUID authorId, InputStream content);
    void updatePageContent(UUID authorId, UUID bookId, Integer pageNumber, InputStream content);
    Page loadPageByBookIdAndPageNumber(UUID bookId, Integer pageNumber);
    Integer getCountOfPages(UUID uuid);
    InputStream loadPageStreamByBookIdAndPageNumber(UUID readerId,UUID bookId, Integer pageNumber);
    void updateBookCover(UUID authorId, UUID uuid, InputStream coverStream, Long coverSize, MediaType type);
    InputStream loadBookCoverStream(UUID authorId, UUID bookId);
    MediaType loadBookCoverContentType();
}
