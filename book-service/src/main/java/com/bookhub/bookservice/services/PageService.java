package com.bookhub.bookservice.services;

import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;

import java.util.List;
import java.util.UUID;

public interface PageService {

    Integer getCountOfPages(UUID bookId);
    Page loadPageByBookIdAndPageNumber(UUID bookId, Integer pageNumber);
    void updatePageFilePath(UUID pageId, String path);
    UUID addNewPageToBook(Book book, int index);
    List<Page> loadBookPagesSortedByPageNumber(UUID bookId);
}
