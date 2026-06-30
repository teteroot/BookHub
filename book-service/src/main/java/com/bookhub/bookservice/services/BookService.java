package com.bookhub.bookservice.services;

import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;

import java.io.InputStream;
import java.util.UUID;

public interface BookService {
    InputStream loadBookStream(UUID authorId, UUID bookId);
    Book loadBookByUUID(UUID uuid);
    void createBook(Book book, UUID authorId);
    void createBookContent(UUID bookId, UUID authorId, InputStream content, Long size);
    Page loadPageByBookIdAndPageNumber(UUID bookId, Integer pageNumber);
    Integer getCountOfPages(UUID uuid);
}
