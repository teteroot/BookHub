package com.bookhub.bookservice.services;

import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;

import java.util.List;
import java.util.UUID;

public interface PageService {

    Integer getCountOfPages(UUID bookId);

    Page loadPageByBookIdAndPageNumber(UUID bookId, Integer pageNumber);

    void updatePageFilePath(UUID pageId, String path);

    UUID addNewPageToBook(Book book, int pageNumber);

    List<Page> loadBookPagesSortedByPageNumber(UUID bookId);

    Page claimPageForUpdate(UUID pageId, UUID bookId);

    Page claimPageForUploadByNumber(UUID bookId, Integer pageNumber);

    void putNewPageToBook(Book book, String pagePath, int pageNumber);

    void deleteBookPage(Page page, UUID bookId);

    void swapPages(Page page, Page swappedPage);

    void movePageTo(Page page, UUID bookId , Integer pageNumber);
}
