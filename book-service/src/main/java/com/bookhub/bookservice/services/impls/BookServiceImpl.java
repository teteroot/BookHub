package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.BookAccessDeniedException;
import com.bookhub.bookservice.exceptions.extensions.ContentSaveException;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.services.BookManagementService;
import com.bookhub.bookservice.services.BookService;
import com.bookhub.bookservice.services.BookStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final BookManagementService bookManagementService;
    private final BookStorageService bookStorageService;

    @Override
    public Book loadBookByUUID(UUID uuid) {
        return bookManagementService.loadBookByUUID(uuid);
    }

    @Override
    public void createBook(Book book, UUID authorId) {
        book.setAuthorId(authorId);
        book.setStatus(BookStatus.DRAFT);
        bookManagementService.createBook(book);
    }

    @Override
    public void updateBookContent(UUID bookId, UUID authorId, InputStream content, Long size) {
        var book = bookManagementService.loadBookByUUID(bookId);
        if (!book.getAuthorId().equals(authorId)){
            throw new BookAccessDeniedException();
        }
        var oldPath = book.getS3ArchivePath();
        var path = bookStorageService.updateContent(bookId, content, size);
        if (oldPath == null) {
            try {
                bookManagementService.updateS3ArchivePath(bookId,path);
            } catch (DataAccessException e) {
                bookStorageService.removeContent(path);
                throw new ContentSaveException();
            }
        }
    }

}
