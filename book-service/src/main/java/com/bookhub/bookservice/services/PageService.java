package com.bookhub.bookservice.services;

import com.bookhub.bookservice.models.Page;

import java.util.List;
import java.util.UUID;

public interface PageService {

    Integer getCountOfPages(UUID bookId);
    List<Page> loadPagesByBookId(UUID bookId);
    Page loadPageByBookIdAndPageNumber(UUID bookId, Integer pageNumber);
}
