package com.bookhub.bookservice.services;

import com.bookhub.bookservice.models.Book;

import java.io.InputStream;
import java.util.UUID;

public interface BookService {

    Book loadBookByUUID(UUID uuid);
    void createBook(Book book, UUID authorId);
    void updateBookContent(UUID bookId, UUID authorId, InputStream content, Long size);
}
