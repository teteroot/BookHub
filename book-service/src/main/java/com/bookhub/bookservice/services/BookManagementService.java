package com.bookhub.bookservice.services;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.models.Book;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface BookManagementService {

    Book loadBookByUUID(UUID uuid);
    Book loadAuthorBookByUUID(UUID bookId, UUID authorId);
    Book claimBookForUpdate(UUID bookId, UUID authorId);

    void createBook(Book book);

    void removeAllPages(UUID bookId);

    void updateBookStatus(UUID bookId, BookStatus bookStatus);

    void updateBookContentPath(UUID bookId,String path);

    void removeContentPath(UUID bookId);

    void updateBookCoverPath(UUID bookId, String coverPath);

    void deleteBookByUUID(UUID bookId);

    boolean isExistAndPublishedBook(UUID bookId);

    void incrementBookStars(UUID bookId, int weight);

    List<Book> loadPublishedBooksByIds(List<UUID> bookIds, UUID authorId);

    Page<Book> loadBooksWithAuthorIdAndStatus(UUID authorId,BookStatus status, Integer page);
}
