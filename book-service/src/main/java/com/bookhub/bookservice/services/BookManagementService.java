package com.bookhub.bookservice.services;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.models.Book;

import java.util.UUID;

public interface BookManagementService {

    Book loadBookByUUID(UUID uuid);
    Book claimBookForUpload(UUID bookId, UUID authorId);
    void createBook(Book book);

    void removeAllPages(UUID bookId);

    void updateBookStatus(UUID bookId, BookStatus bookStatus);

    void updateBookContentPath(UUID bookId,String path);
}
