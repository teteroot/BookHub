package com.bookhub.bookservice.orchestrators;

import com.bookhub.bookservice.models.Page;
import org.springframework.http.MediaType;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface BookMediaOrchestrator {

    void createBookContent(UUID bookId, UUID authorId, InputStream content);

    InputStream loadBookStream(UUID bookId, UUID authorId);

    InputStream loadBooksArchiveStream(List<UUID> bookIds, UUID authorId);

    void updateBookCover(UUID bookId, UUID authorId, InputStream coverStream, Long coverSize, MediaType type);

    InputStream loadBookCoverStream(UUID bookId, UUID authorId);

    MediaType loadBookCoverContentType();

    void removeBookContent(UUID bookId);

    String cacheBookContent(UUID bookId);

    String cacheBookCoverFromFirstPage(UUID bookId);

    record PageContent(InputStream stream, UUID pageId) {}
    Page loadPageByBookIdAndPageNumber(UUID bookId, Integer pageNumber);

    PageContent loadPageStreamByBookIdAndPageNumber(UUID bookId, UUID readerId, Integer pageNumber);

    void createBookPage(UUID bookId, UUID authorId, Integer pageNumber, InputStream content);

    void updatePageContent(UUID bookId, UUID authorId, UUID pageId, InputStream content);

    void deleteBookPage(UUID bookId, UUID authorId, UUID pageId);

}
