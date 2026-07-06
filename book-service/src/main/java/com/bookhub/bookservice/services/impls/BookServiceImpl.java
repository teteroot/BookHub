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
import java.util.ArrayList;
import java.util.List;
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
        var pages = bookManagementService.loadBookPagesByUUID(bookId);
        if (book.getStatus().equals(BookStatus.DRAFT) && !book.getAuthorId().equals(authorId)){
            throw new BookAccessDeniedException();
        }

        List<InputStream> pagesStreams = new ArrayList<>();
        pages.forEach((page) -> pagesStreams.add(bookStorageService.loadPageContent(page.getS3FilePath())));
        return pdfService.collectBookFromPages(pagesStreams);
    }

    @Override
    public Book loadBookByUUID(UUID uuid) {
        return bookManagementService.loadBookByUUID(uuid);
    }

    @Override
    public void createBook(Book book, UUID authorId) {
        book.setAuthorId(authorId);
        book.setStatus(BookStatus.EMPTY);
        bookManagementService.createBook(book);
    }

    @Override
    public void createBookContent(UUID bookId, UUID authorId, InputStream content) {

        var book = bookManagementService.loadBookByUUID(bookId);
        if (!book.getStatus().equals(BookStatus.EMPTY)) {
            throw new BookContentAlreadyExistException();
        }
        if (!book.getAuthorId().equals(authorId)){
            throw new BookAccessDeniedException();
        }
        if (pageService.getCountOfPages(bookId) > 0){
            removeAllBookPages(bookId);
        }
        var pagesSteams = pdfService.loadPagesStreams(content);
        int iterator = 0;
        for (InputStream page: pagesSteams.keySet()) {

            try(InputStream stream = new BufferedInputStream(page)) {
                var pageId = pageService.addNewPageToBook(book, iterator++);
                var path = bookStorageService.createPageContent(bookId, pageId, stream, pagesSteams.get(page));
                pageService.updatePageFilePath(pageId, path);
            } catch (Exception e){
                removeAllBookPages(bookId);
                throw new ContentSaveException();
            }
        }
        bookManagementService.updateBookStatus(bookId, BookStatus.DRAFT);

    }

    private void removeAllBookPages(UUID bookId) {
        try {
            bookManagementService.removeAllPages(bookId);
        } catch (Exception __ignore){
            log.error("Error while removing pages from book with id: {}", bookId);
        }
        try {
            bookStorageService.removeBookContent(bookId);
        } catch (Exception __ignore) {
            log.error("Error while removing pages from book with id: {}", bookId);
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
