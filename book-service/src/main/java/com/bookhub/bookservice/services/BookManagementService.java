package com.bookhub.bookservice.services;

import com.bookhub.bookservice.models.Book;

import java.util.UUID;

public interface BookManagementService {

    Book loadBookByUUID(UUID uuid);
    void createBook(Book book);

    void addNewBookContent(UUID bookId, String path, int countOfPages);
}
