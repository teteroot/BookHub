package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.*;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;
import com.bookhub.bookservice.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookOrchestratorImpl implements BookOrchestrator {

    private final BookManagementService bookManagementService;
    private final BookStorageService bookStorageService;
    private final PDFService pdfService;
    private final PageService pageService;
    private final FileTempService fileTempService;

    @Override
    public InputStream loadBookStream(UUID authorId, UUID bookId) {
        var book = bookManagementService.loadBookByUUID(bookId);
        if (book.getStatus().equals(BookStatus.DRAFT) && !book.getAuthorId().equals(authorId)){
            throw new BookAccessDeniedException();
        }
        var path = book.getS3ArchivePath();
        if (path == null){
            path = cacheBookContent(bookId);
        }
        return bookStorageService.loadContent(path);
    }

    @Override
    public InputStream loadPageStreamByBookIdAndPageNumber(UUID readerId,UUID bookId, Integer pageNumber) {
        var book = bookManagementService.loadBookByUUID(bookId);
        if (book.getStatus().equals(BookStatus.DRAFT) && !book.getAuthorId().equals(readerId)){
            throw new BookAccessDeniedException();
        }
        var page = pageService.loadPageByBookIdAndPageNumber(bookId, pageNumber);
        return bookStorageService.loadContent(page.getS3FilePath());
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
        var book = bookManagementService.claimBookForUpload(bookId, authorId);
        if (pageService.getCountOfPages(bookId) > 0){
            removeAllBookPages(bookId);
        }
        List<Path> pages = null;
        try {
            pages = pdfService.loadPages(content);
            for (int i = 0;i<pages.size();i++) {
                try(InputStream stream = fileTempService.openStream(pages.get(i))) {
                    var pageId = pageService.addNewPageToBook(book, i);
                    var path = bookStorageService.createPageContent(bookId, pageId, stream, fileTempService.sizeOf(pages.get(i)));
                    pageService.updatePageFilePath(pageId, path);
                }
            }
        } catch (Exception e) {
            removeAllBookPages(bookId);
            log.error("Error while creating book content for bookId: {}", bookId, e);
            throw new ContentSaveException();
        } finally {
            if (pages != null) {
                fileTempService.deleteQuietly(pages);
            }
        }

        bookManagementService.updateBookStatus(bookId, BookStatus.DRAFT);

    }

    @Override
    public void updatePageContent(UUID authorId, UUID bookId, Integer pageNumber, InputStream content) {
        var book = bookManagementService.loadBookByUUID(bookId);
        if (!book.getAuthorId().equals(authorId)){
            throw new BookAccessDeniedException();
        }
        if (!book.getStatus().equals(BookStatus.DRAFT)){
            throw new BookNotDraftingException();
        }
        var page = pageService.claimPageForUpload(bookId, pageNumber);
        var pageFiles = pdfService.loadPages(content);
        try {
            if (pageFiles.size() != 1){
                throw new TooManyPagesException(1);
            }
            bookManagementService.removeContentPath(bookId);
            var pagePath = pageFiles.getFirst();
            try(InputStream pageStream = fileTempService.openStream(pagePath)) {
                long pageSize = fileTempService.sizeOf(pagePath);
                bookStorageService.updatePageContent(page.getS3FilePath(), pageStream, pageSize);
            }
        } catch (TooManyPagesException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update page {} content for bookId {}", pageNumber, bookId, e);
            throw new ContentSaveException();
        } finally {
            fileTempService.deleteQuietly(pageFiles);
        }
    }

    private void removeAllBookPages(UUID bookId) {
        try {
            bookManagementService.removeAllPages(bookId);
        } catch (Exception ignored){}
        try {
            bookStorageService.removeBook(bookId);
        } catch (Exception ignored) {}
    }

    @Override
    public Page loadPageByBookIdAndPageNumber(UUID bookId, Integer pageNumber) {
        return pageService.loadPageByBookIdAndPageNumber(bookId, pageNumber);
    }


    @Override
    public Integer getCountOfPages(UUID uuid) {
        return pageService.getCountOfPages(uuid);
    }

    private String cacheBookContent(UUID bookId){
        var pages = pageService.loadBookPagesSortedByPageNumber(bookId);
        if (pages.isEmpty()){
            throw new BookContentNotFoundException();
        }
        List<Path> pageFiles = new ArrayList<>();
        Path bookPath = null;
        try {
            for (Page page: pages){
                pageFiles.add(fileTempService.writeToTempFile("page-", ".pdf", os -> {
                    try(InputStream stream = bookStorageService.loadContent(page.getS3FilePath())) {
                        stream.transferTo(os);
                    }
                }));
            }
            bookPath = pdfService.collectBookFromPages(pageFiles);
            try(InputStream fileStream = fileTempService.openStream(bookPath)) {
                long bookSize = fileTempService.sizeOf(bookPath);
                var path = bookStorageService.createBookContent(bookId,fileStream, bookSize);
                bookManagementService.updateBookContentPath(bookId, path);
                return path;
            }
        } catch (Exception e) {
            throw new ContentLoadException();
        } finally {
            if (bookPath != null) pageFiles.add(bookPath);
            fileTempService.deleteQuietly(pageFiles);
        }
    }
}
