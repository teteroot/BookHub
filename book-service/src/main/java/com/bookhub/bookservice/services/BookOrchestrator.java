package com.bookhub.bookservice.services;

import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;
import org.springframework.http.MediaType;

import java.io.InputStream;
import java.util.UUID;

public interface BookOrchestrator {

    InputStream loadBookStream(UUID bookId, UUID authorId);

    Book loadBookByUUID(UUID bookId,UUID authorId);

    void createBook(Book book, UUID authorId);

    void createBookContent(UUID bookId, UUID authorId, InputStream content);

    void updatePageContent(UUID bookId, UUID authorId, UUID pageId, InputStream content);

    Page loadPageByBookIdAndPageNumber(UUID bookId, Integer pageNumber);

    Integer getCountOfPages(UUID bookId);

    void createBookPage(UUID bookId, UUID authorId, Integer pageNumber, InputStream content);

    void deleteBookPage(UUID bookId, UUID authorId, UUID pageId);

    void swapBookPages(UUID bookId, UUID authorId, UUID pageId, UUID swapPageId);

    void moveBookPage(UUID bookId, UUID authorId, UUID pageId, Integer pageNumber);

    record PageContent(InputStream stream, UUID pageId) {}
    PageContent loadPageStreamByBookIdAndPageNumber(UUID bookId,UUID readerId, Integer pageNumber);

    void updateBookCover(UUID bookId, UUID authorId, InputStream coverStream, Long coverSize, MediaType type);

    InputStream loadBookCoverStream(UUID bookId, UUID authorId);

    MediaType loadBookCoverContentType();

    void publishBook(UUID bookId, UUID authorId);

    void draftBook(UUID bookId, UUID authorId);

    void archiveBook(UUID bookId, UUID authorId);

    void deleteBook(UUID bookId, UUID authorId);
}
