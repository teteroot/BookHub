package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.*;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;
import com.bookhub.bookservice.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    private final ImageService imageService;

    @Override
    public InputStream loadBookStream(UUID bookId, UUID authorId) {
        var book = bookManagementService.loadBookByUUID(bookId);
        if (!book.getStatus().equals(BookStatus.PUBLISHED) && !book.getAuthorId().equals(authorId)){
            throw new BookAccessDeniedException();
        }
        var path = book.getS3ArchivePath();
        if (path == null){
            path = cacheBookContent(bookId);
        }
        return bookStorageService.loadContent(path);
    }

    @Override
    public PageContent loadPageStreamByBookIdAndPageNumber(UUID bookId,UUID readerId, Integer pageNumber) {
        var book = bookManagementService.loadBookByUUID(bookId);
        if (!book.getStatus().equals(BookStatus.PUBLISHED) && !book.getAuthorId().equals(readerId)){
            throw new BookAccessDeniedException();
        }
        var page = pageService.loadPageByBookIdAndPageNumber(bookId, pageNumber);
        return new PageContent(bookStorageService.loadContent(page.getS3FilePath()), page.getId());
    }

    @Override
    public void updateBookCover(UUID bookId, UUID authorId, InputStream coverStream, Long coverSize, MediaType type) {
        var book = bookManagementService.claimBookForUpdate(bookId, authorId);
        if (!book.getStatus().equals(BookStatus.DRAFT)) {
            throw new BookNotDraftingException();
        }
        var oldCoverPath = book.getS3CoverPath();
        var commonType = imageService.getCommonCoverType();
        var extension = ".%s".formatted(commonType.getSubtype());
        String coverPath;
        if (type.equals(commonType)){
            coverPath = updateCover(book.getId(), extension, coverStream, coverSize, type);
        }else {
            List<Path> tempFiles = new ArrayList<>();
            try {
                var tempFile = fileTempService.writeToTempFile("cover-",
                        extension,
                        os -> imageService.convertToCommonFormat(coverStream, os)
                );
                tempFiles.add(tempFile);
                try(InputStream stream = fileTempService.openStream(tempFile)) {
                    coverPath = updateCover(book.getId(),
                            extension,
                            stream,
                            fileTempService.sizeOf(tempFile),
                            commonType
                    );
                }
            } catch (IOException e) {
                throw new CoverSaveException();
            }
            finally {
                fileTempService.deleteQuietly(tempFiles);
            }
        }
        if (oldCoverPath != null &&  !oldCoverPath.equals(coverPath)){
            bookStorageService.removeBookStorageContent(oldCoverPath);
        }
    }

    private String updateCover(UUID bookId, String extension, InputStream coverStream, Long coverSize, MediaType type){
        var coverPath = bookStorageService.createBookCover(bookId,extension, type.toString(), coverStream , coverSize);
        try {
            bookManagementService.updateBookCoverPath(bookId, coverPath);
        } catch (DataAccessException e) {
            bookStorageService.removeBookStorageContent(coverPath);
            log.error("Failed to update book path at {}", bookId, e);
            throw new CoverSaveException();
        }
        return coverPath;
    }

    @Override
    public InputStream loadBookCoverStream(UUID bookId, UUID authorId) {
        var book = bookManagementService.loadBookByUUID(bookId);
        if (!book.getStatus().equals(BookStatus.PUBLISHED) && !book.getAuthorId().equals(authorId)){
            throw new BookAccessDeniedException();
        }
        var path = book.getS3CoverPath();
        if (path == null){
            path = cacheBookCoverFromFirstPage(bookId, imageService.getCommonCoverType());
        }
        return bookStorageService.loadContent(path);
    }

    @Override
    public MediaType loadBookCoverContentType() {
        return imageService.getCommonCoverType();
    }

    @Override
    public void publishBook(UUID bookId, UUID authorId) {
        var book = bookManagementService.loadAuthorBookByUUID(bookId, authorId);
        if (!book.getStatus().equals(BookStatus.DRAFT)) {
            throw new BookNotDraftingException(book.getStatus());
        }
        if (pageService.getCountOfPages(book.getId()) == 0){
            throw new BookContentNotFoundException();
        }
        if (book.getS3ArchivePath() == null){
            cacheBookContent(book.getId());
        }
        if (book.getS3CoverPath() == null) {
            cacheBookCoverFromFirstPage(book.getId(), imageService.getCommonCoverType());
        }
        bookManagementService.updateBookStatus(book.getId(), BookStatus.PUBLISHED);
    }

    @Override
    public void draftBook(UUID bookId, UUID authorId) {
        var book = bookManagementService.loadAuthorBookByUUID(bookId, authorId);
        if (book.getStatus().equals(BookStatus.DRAFT)) {
            throw new BookAlreadyRequireStatusException(BookStatus.DRAFT);
        }
        if (book.getStatus().equals(BookStatus.EMPTY)){
            throw new BookContentNotFoundException();
        }
        bookManagementService.updateBookStatus(book.getId(), BookStatus.DRAFT);
    }

    @Override
    public void archiveBook(UUID bookId, UUID authorId) {
        var book = bookManagementService.loadAuthorBookByUUID(bookId, authorId);
        if (book.getStatus().equals(BookStatus.ARCHIVED)) {
            throw new BookAlreadyRequireStatusException(BookStatus.ARCHIVED);
        }
        if (book.getStatus().equals(BookStatus.EMPTY)){
            throw new BookContentNotFoundException();
        }
        bookManagementService.updateBookStatus(book.getId(), BookStatus.ARCHIVED);
    }

    @Override
    public void deleteBook(UUID bookId, UUID authorId) {
        bookManagementService.loadAuthorBookByUUID(bookId,authorId);
        bookManagementService.deleteBookByUUID(bookId);
        bookStorageService.removeBook(bookId);
    }

    @Override
    public Book loadBookByUUID(UUID bookId,UUID authorId) {
        var book = bookManagementService.loadBookByUUID(bookId);
        if (!book.getAuthorId().equals(authorId) && !book.getStatus().equals(BookStatus.PUBLISHED)){
            throw new BookAccessDeniedException();
        }
        return book;
    }

    @Override
    public void createBook(Book book, UUID authorId) {
        book.setAuthorId(authorId);
        book.setStatus(BookStatus.EMPTY);
        bookManagementService.createBook(book);
    }

    @Override
    public void createBookContent(UUID bookId, UUID authorId, InputStream content) {
        var book = bookManagementService.claimBookForUpdate(bookId, authorId);
        if (!book.getStatus().equals(BookStatus.EMPTY)) {
            throw new BookContentAlreadyExistException();
        }
        if (pageService.getCountOfPages(bookId) > 0){
            removeAllBookPages(bookId);
        }
        List<Path> pages = null;
        try {
            pages = pdfService.loadPages(content);
            for (int i = 0;i<pages.size();i++) {
                try(InputStream stream = fileTempService.openStream(pages.get(i))) {
                    var pageId = pageService.addNewPageToBook(book, i + 1);
                    var path = bookStorageService.createPageContent(bookId, stream, fileTempService.sizeOf(pages.get(i)));
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
    public void updatePageContent(UUID bookId, UUID authorId, UUID pageId, InputStream content) {
        var book = bookManagementService.loadAuthorBookByUUID(bookId, authorId);
        if (!book.getStatus().equals(BookStatus.DRAFT)){
            throw new BookNotDraftingException();
        }
        var page = pageService.claimPageForUpload(pageId,bookId);
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
            log.error("Failed to update page {} content for bookId {}", pageId, bookId, e);
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
    public Integer getCountOfPages(UUID bookId) {
        return pageService.getCountOfPages(bookId);
    }

    @Override
    public void createBookPage(UUID bookId, UUID authorId, Integer pageNumber, InputStream content) {
        var book = bookManagementService.claimBookForUpdate(bookId ,authorId);
        if (!book.getStatus().equals(BookStatus.DRAFT)){
            throw new BookNotDraftingException();
        }
        if (pageNumber == null){
            pageNumber = pageService.getCountOfPages(bookId) + 1;
        }
        List<Path> pageFiles = new ArrayList<>();
        String pageContentPath = "";
        try {
            pageFiles.addAll(pdfService.loadPages(content));
            if (pageFiles.size() != 1){
                throw new TooManyPagesException(1);
            }
            bookManagementService.removeContentPath(bookId);
            var pagePath = pageFiles.getFirst();
            try(InputStream pageStream = fileTempService.openStream(pagePath)) {
                long pageSize = fileTempService.sizeOf(pagePath);
                pageContentPath = bookStorageService.createPageContent(bookId,  pageStream, pageSize);
                pageService.putNewPageToBook(book, pageContentPath, pageNumber);
            }
        } catch (TooManyPagesException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update page content for bookId {}", bookId, e);
            if (!pageContentPath.isBlank()){
                bookStorageService.removeBookStorageContent(pageContentPath);
            }
            throw new ContentSaveException();
        } finally {
            fileTempService.deleteQuietly(pageFiles);
        }
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

    private String cacheBookCoverFromFirstPage(UUID bookId, MediaType type) {
        Page page;
        try {
            page = pageService.claimPageForUploadByNumber(bookId, 1);
        } catch (PageNotFoundException e) {
            throw new BookContentNotFoundException();
        }
        var tempFiles = new ArrayList<Path>();
        try {
            var pageFile = fileTempService.writeToTempFile("page-", ".pdf", os -> {
                try(InputStream stream = bookStorageService.loadContent(page.getS3FilePath())) {
                    stream.transferTo(os);
                }
            });
            var imageFile = pdfService.convertSinglePageToImage(pageFile,type, imageService.getPdfDpi());
            tempFiles.addAll(List.of(pageFile,  imageFile));
            try (InputStream coverStream = fileTempService.openStream(imageFile)) {
                return updateCover(bookId,
                        ".%s".formatted(type.getSubtype()),
                        coverStream,
                        fileTempService.sizeOf(imageFile),
                        type
                );
            }
        } catch (Exception e) {
            throw new CoverLoadException();
        } finally {
            fileTempService.deleteQuietly(tempFiles);
        }

    }
}
