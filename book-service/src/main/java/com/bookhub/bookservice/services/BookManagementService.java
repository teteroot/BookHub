package com.bookhub.bookservice.services;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;

import java.util.List;
import java.util.UUID;

public interface BookManagementService {

    Book loadBookByUUID(UUID uuid);
    void createBook(Book book);
    List<Page> loadBookPagesByUUID(UUID bookId);

    void removeAllPages(UUID bookId);

    void updateBookStatus(UUID bookId, BookStatus bookStatus);
}
