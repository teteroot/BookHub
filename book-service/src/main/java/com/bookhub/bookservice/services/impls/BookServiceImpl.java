package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.BookAccessDeniedException;
import com.bookhub.bookservice.exceptions.extensions.BookContentAlreadyExistException;
import com.bookhub.bookservice.exceptions.extensions.ContentSaveException;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;
import com.bookhub.bookservice.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final BookManagementService bookManagementService;
    private final BookStorageService bookStorageService;
    private final PDFService pdfService;
    private final PageService pageService;

    @Override
    public InputStream loadBookStream(UUID authorId, UUID bookId) {
        var book = bookManagementService.loadBookByUUID(bookId);
        if (book.getStatus().equals(BookStatus.DRAFT) && !book.getAuthorId().equals(authorId)){
            throw new BookAccessDeniedException();
        }
        return bookStorageService.loadContent(book.getS3ArchivePath());
    }

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
    public void createBookContent(UUID bookId, UUID authorId, InputStream content, Long size) {
        var book = bookManagementService.loadBookByUUID(bookId);
        if (book.getS3ArchivePath() != null) {
            throw new BookContentAlreadyExistException();
        }
        if (!book.getAuthorId().equals(authorId)){
            throw new BookAccessDeniedException();
        }
        var path = bookStorageService.createContent(bookId, new BufferedInputStream(content), size);
        try (InputStream pdfContent =
                     new BufferedInputStream(bookStorageService.loadContent(path))){
            Integer countOfPages = pdfService.countOfPages(pdfContent);
            bookManagementService.addNewBookContent(bookId,path, countOfPages);
        } catch (Exception e) {
            bookStorageService.removeContent(path);
            throw new ContentSaveException();
        }
    }

    @Override
    public Page loadPageByBookIdAndPageNumber(UUID bookId, Integer pageNumber) {
        return pageService.loadPageByBookIdAndPageNumber(bookId, pageNumber);
    }


    @Override
    public Integer getCountOfPages(UUID uuid) {
        return pageService.getCountOfPages(uuid);
    }
}
