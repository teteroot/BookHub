package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.*;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;
import com.bookhub.bookservice.services.BookManagementService;
import com.bookhub.bookservice.services.BookStorageService;
import com.bookhub.bookservice.services.FileTempService;
import com.bookhub.bookservice.services.PDFService;
import com.bookhub.bookservice.services.PageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookOrchestratorImplTest {

    @Mock
    private BookManagementService bookManagementService;
    @Mock
    private BookStorageService bookStorageService;
    @Mock
    private PDFService pdfService;
    @Mock
    private PageService pageService;
    @Mock
    private FileTempService fileTempService;

    @InjectMocks
    private BookOrchestratorImpl bookService;

    @Test
    void testLoadForbiddenBookStream() {
        var book = Book.builder()
                .id(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .status(BookStatus.DRAFT)
                .build();
        when(bookManagementService.loadBookByUUID(book.getId())).thenReturn(book);
        assertThrows(BookAccessDeniedException.class,
                () -> bookService.loadBookStream(UUID.randomUUID(), book.getId()));
    }

    @Test
    void testSuccessfulLoadBookStream_cacheMiss_generatesAndCachesArchive() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();

        var book = Book.builder()
                .id(bookId)
                .authorId(authorId)
                .status(BookStatus.DRAFT)
                .s3ArchivePath(null)
                .build();

        when(bookManagementService.loadBookByUUID(bookId)).thenReturn(book);

        var page = Page.builder().id(UUID.randomUUID()).s3FilePath("page-path").build();
        when(pageService.loadBookPagesSortedByPageNumber(bookId)).thenReturn(List.of(page));
        var pageTempPath = Path.of("page-temp.pdf");
        var bookTempPath = Path.of("book-temp.pdf");
        when(fileTempService.writeToTempFile(eq("page-"), eq(".pdf"), any())).thenReturn(pageTempPath);
        when(fileTempService.openStream(bookTempPath)).thenReturn(InputStream.nullInputStream());
        when(fileTempService.sizeOf(bookTempPath)).thenReturn(20L);
        when(pdfService.collectBookFromPages(anyList())).thenReturn(bookTempPath);
        when(bookStorageService.createBookContent(eq(bookId), any(), eq(20L)))
                .thenReturn("archive-path");
        when(bookStorageService.loadContent("archive-path")).thenReturn(InputStream.nullInputStream());

        assertDoesNotThrow(() -> bookService.loadBookStream(authorId, bookId));

        verify(bookManagementService).updateBookContentPath(bookId, "archive-path");
        verify(bookManagementService).loadBookByUUID(bookId);
        verify(bookStorageService).loadContent("archive-path");
    }

    @Test
    void testLoadBookStream_cacheHit_skipsRegeneration() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var book = Book.builder()
                .id(bookId)
                .authorId(authorId)
                .status(BookStatus.DRAFT)
                .s3ArchivePath("archive-path")
                .build();

        when(bookManagementService.loadBookByUUID(bookId)).thenReturn(book);
        when(bookStorageService.loadContent("archive-path")).thenReturn(InputStream.nullInputStream());

        assertDoesNotThrow(() -> bookService.loadBookStream(authorId, bookId));

        verifyNoInteractions(fileTempService);
        verify(pageService, never()).loadBookPagesSortedByPageNumber(any());
        verify(pdfService, never()).collectBookFromPages(any());
        verify(bookManagementService, never()).updateBookContentPath(any(), any());
    }

    @Test
    void testLoadBookStream_cacheMiss_emptyBook_throwsBookContentNotFoundException() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var book = Book.builder()
                .id(bookId)
                .authorId(authorId)
                .status(BookStatus.DRAFT)
                .s3ArchivePath(null)
                .build();

        when(bookManagementService.loadBookByUUID(bookId)).thenReturn(book);
        when(pageService.loadBookPagesSortedByPageNumber(bookId)).thenReturn(Collections.emptyList());

        assertThrows(BookContentNotFoundException.class,
                () -> bookService.loadBookStream(authorId, bookId));

        verifyNoInteractions(fileTempService);
        verify(bookStorageService, never()).createBookContent(any(), any(), anyLong());
        verify(bookManagementService, never()).updateBookContentPath(any(), any());
    }

    @Test
    void testLoadBookStream_publishedBookAccessibleByAnyone() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var path = Path.of("book-temp.pdf");
        var bookWithoutArchive = Book.builder()
                .id(bookId)
                .authorId(authorId)
                .status(BookStatus.PUBLISHED)
                .s3ArchivePath(null)
                .build();
        when(bookManagementService.loadBookByUUID(bookId))
                .thenReturn(bookWithoutArchive);
        when(pageService.loadBookPagesSortedByPageNumber(bookId))
                .thenReturn(List.of(Page.builder()
                        .id(UUID.randomUUID())
                        .s3FilePath("path")
                        .build()));
        when(fileTempService.writeToTempFile(eq("page-"), eq(".pdf"), any())).thenReturn(Path.of("page-temp.pdf"));
        when(fileTempService.openStream(path)).thenReturn(InputStream.nullInputStream());
        when(fileTempService.sizeOf(path)).thenReturn(20L);
        when(pdfService.collectBookFromPages(anyList())).thenReturn(path);
        when(bookStorageService.createBookContent(eq(bookId), any(), eq(20L))).thenReturn("archive-path");
        when(bookStorageService.loadContent("archive-path")).thenReturn(InputStream.nullInputStream());
        assertDoesNotThrow(() -> bookService.loadBookStream(UUID.randomUUID(), bookId));
    }

    @Test
    void testLoadBookStream_bookNotFound() {
        var bookId = UUID.randomUUID();
        when(bookManagementService.loadBookByUUID(bookId)).thenThrow(new BookNotFoundException());

        assertThrows(BookNotFoundException.class,
                () -> bookService.loadBookStream(UUID.randomUUID(), bookId));
        verifyNoInteractions(bookStorageService, pdfService);
    }

    @Test
    void testLoadBookStream_noPages_BookContentNotFoundException() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var bookWithoutArchive = Book.builder()
                .id(bookId)
                .authorId(authorId)
                .status(BookStatus.PUBLISHED)
                .s3ArchivePath(null)
                .build();

        when(bookManagementService.loadBookByUUID(bookId)).thenReturn(bookWithoutArchive);
        when(pageService.loadBookPagesSortedByPageNumber(bookId)).thenReturn(Collections.emptyList());

        assertThrows(BookContentNotFoundException.class,() -> bookService.loadBookStream(authorId, bookId));
        verifyNoInteractions(bookStorageService, fileTempService);
    }

    @Test
    void testLoadBookByUUID_delegatesToManagementService() {
        var book = Book.builder().id(UUID.randomUUID()).build();
        when(bookManagementService.loadBookByUUID(book.getId())).thenReturn(book);

        var result = bookService.loadBookByUUID(book.getId());

        assertEquals(book, result);
        verify(bookManagementService).loadBookByUUID(book.getId());
    }

    @Test
    void testLoadBookByUUID_notFound() {
        var bookId = UUID.randomUUID();
        when(bookManagementService.loadBookByUUID(bookId)).thenThrow(new BookNotFoundException());

        assertThrows(BookNotFoundException.class, () -> bookService.loadBookByUUID(bookId));
    }

    @Test
    void testCreateBook_setsAuthorAndEmptyStatus() {
        var book = Book.builder().build();
        var authorId = UUID.randomUUID();

        bookService.createBook(book, authorId);

        assertEquals(authorId, book.getAuthorId());
        assertEquals(BookStatus.EMPTY, book.getStatus());
        verify(bookManagementService).createBook(book);
    }

    @Test
    void testCreateBook_alreadyExists_propagatesException() {
        var book = Book.builder().title("Duplicate").build();
        doThrow(new BookAlreadyExistException(book.getTitle()))
                .when(bookManagementService).createBook(book);

        assertThrows(BookAlreadyExistException.class,
                () -> bookService.createBook(book, UUID.randomUUID()));
    }

    @Test
    void testCreateBookContent_success_noExistingPages() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var book = Book.builder().id(bookId).authorId(authorId).build();
        var pageStream = InputStream.nullInputStream();
        var path = Path.of("page-1.pdf");

        when(bookManagementService.claimBookForUpload(bookId, authorId)).thenReturn(book);
        when(pageService.getCountOfPages(bookId)).thenReturn(0);
        when(pdfService.loadPages(any())).thenReturn(List.of(path));
        when(fileTempService.openStream(path)).thenReturn(pageStream);
        when(fileTempService.sizeOf(path)).thenReturn(123L);
        when(pageService.addNewPageToBook(eq(book), anyInt())).thenReturn(UUID.randomUUID());
        when(bookStorageService.createPageContent(eq(bookId), any(), any(), eq(123L))).thenReturn("path");

        assertDoesNotThrow(() -> bookService.createBookContent(bookId, authorId, InputStream.nullInputStream()));

        verify(bookManagementService, never()).removeAllPages(any());
        verify(pageService).updatePageFilePath(any(), eq("path"));
        verify(bookManagementService).updateBookStatus(bookId, BookStatus.DRAFT);
    }

    @Test
    void testCreateBookContent_removesExistingPagesBeforeUpload() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var book = Book.builder().id(bookId).authorId(authorId).build();

        when(bookManagementService.claimBookForUpload(bookId, authorId)).thenReturn(book);
        when(pageService.getCountOfPages(bookId)).thenReturn(5);
        when(pdfService.loadPages(any())).thenReturn(Collections.emptyList());

        bookService.createBookContent(bookId, authorId, InputStream.nullInputStream());

        verify(bookManagementService).removeAllPages(bookId);
        verify(bookStorageService).removeBook(bookId);
        verify(bookManagementService).updateBookStatus(bookId, BookStatus.DRAFT);
    }

    @Test
    void testCreateBookContent_multiplePages_incrementsPageNumberSequentially() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var book = Book.builder().id(bookId).authorId(authorId).build();

        var stream1 = InputStream.nullInputStream();
        var stream2 = InputStream.nullInputStream();
        var path1 = Path.of("page-1.pdf");
        var path2 = Path.of("page-2.pdf");
        when(bookManagementService.claimBookForUpload(bookId, authorId)).thenReturn(book);
        when(pageService.getCountOfPages(bookId)).thenReturn(5);
        when(pdfService.loadPages(any())).thenReturn(List.of(path1, path2));
        when(fileTempService.openStream(path1)).thenReturn(stream1);
        when(fileTempService.openStream(path2)).thenReturn(stream2);
        when(fileTempService.sizeOf(path1)).thenReturn(100L);
        when(fileTempService.sizeOf(path2)).thenReturn(200L);
        when(pageService.addNewPageToBook(eq(book), anyInt())).thenReturn(UUID.randomUUID());
        when(bookStorageService.createPageContent(eq(bookId), any(), any(), anyLong())).thenReturn("path");

        bookService.createBookContent(bookId, authorId, InputStream.nullInputStream());

        var pageNumberCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(pageService, times(2)).addNewPageToBook(eq(book), pageNumberCaptor.capture());
        assertEquals(List.of(0, 1), pageNumberCaptor.getAllValues());
    }

    @Test
    void testCreateBookContent_storageFailure_triggersCompensationAndThrows() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var book = Book.builder().id(bookId).authorId(authorId).build();
        var pageStream = InputStream.nullInputStream();
        var path =  Path.of("page-1.pdf");
        when(bookManagementService.claimBookForUpload(bookId, authorId)).thenReturn(book);
        when(pageService.getCountOfPages(bookId)).thenReturn(0);
        when(pdfService.loadPages(any())).thenReturn(List.of(path));
        when(fileTempService.openStream(path)).thenReturn(pageStream);
        when(fileTempService.sizeOf(path)).thenReturn(123L);
        when(pageService.addNewPageToBook(eq(book), anyInt())).thenReturn(UUID.randomUUID());
        when(bookStorageService.createPageContent(eq(bookId), any(), any(), eq(123L)))
                .thenThrow(new ContentSaveException());

        assertThrows(ContentSaveException.class,
                () -> bookService.createBookContent(bookId, authorId, InputStream.nullInputStream()));

        verify(bookManagementService).removeAllPages(bookId);
        verify(bookStorageService).removeBook(bookId);
        verify(bookManagementService, never()).updateBookStatus(any(), any());
    }

    @Test
    void testCreateBookContent_compensationItselfFails_stillThrowsContentSaveException() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var book = Book.builder().id(bookId).authorId(authorId).build();
        var pageStream = InputStream.nullInputStream();
        var path = Path.of("page-1.pdf");

        when(bookManagementService.claimBookForUpload(bookId, authorId)).thenReturn(book);
        when(pageService.getCountOfPages(bookId)).thenReturn(0);
        when(pdfService.loadPages(any())).thenReturn(List.of(path));
        when(fileTempService.openStream(path)).thenReturn(pageStream);
        when(fileTempService.sizeOf(path)).thenReturn(123L);
        when(pageService.addNewPageToBook(eq(book), anyInt())).thenReturn(UUID.randomUUID());
        when(bookStorageService.createPageContent(eq(bookId), any(), any(), eq(123L)))
                .thenThrow(new RuntimeException("S3 unavailable"));
        doThrow(new RuntimeException("DB also down")).when(bookManagementService).removeAllPages(bookId);

        assertThrows(ContentSaveException.class,
                () -> bookService.createBookContent(bookId, authorId, InputStream.nullInputStream()));

        verify(bookStorageService).removeBook(bookId);
    }

    @Test
    void testCreateBookContent_accessDenied_propagatesFromClaim() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        when(bookManagementService.claimBookForUpload(bookId, authorId))
                .thenThrow(new BookAccessDeniedException());

        assertThrows(BookAccessDeniedException.class,
                () -> bookService.createBookContent(bookId, authorId, InputStream.nullInputStream()));

        verifyNoInteractions(pdfService, bookStorageService, pageService);
    }

    @Test
    void testCreateBookContent_emptyPdf_noPagesProcessed() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var book = Book.builder().id(bookId).authorId(authorId).build();

        when(bookManagementService.claimBookForUpload(bookId, authorId)).thenReturn(book);
        when(pageService.getCountOfPages(bookId)).thenReturn(0);
        when(pdfService.loadPages(any())).thenReturn(Collections.emptyList());

        bookService.createBookContent(bookId, authorId, InputStream.nullInputStream());

        verify(pageService, never()).addNewPageToBook(any(), anyInt());
        verify(bookManagementService).updateBookStatus(bookId, BookStatus.DRAFT);
    }

    @Test
    void testLoadPageByBookIdAndPageNumber_success() {
        var bookId = UUID.randomUUID();
        var page = Page.builder().id(UUID.randomUUID()).pageNumber(3).build();
        when(pageService.loadPageByBookIdAndPageNumber(bookId, 3)).thenReturn(page);

        var result = bookService.loadPageByBookIdAndPageNumber(bookId, 3);

        assertEquals(page, result);
    }

    @Test
    void testLoadPageByBookIdAndPageNumber_notFound() {
        var bookId = UUID.randomUUID();
        when(pageService.loadPageByBookIdAndPageNumber(bookId, 99))
                .thenThrow(new PageNotFoundException());

        assertThrows(PageNotFoundException.class,
                () -> bookService.loadPageByBookIdAndPageNumber(bookId, 99));
    }

    @Test
    void testGetCountOfPages_delegatesToPageService() {
        var bookId = UUID.randomUUID();
        when(pageService.getCountOfPages(bookId)).thenReturn(42);

        var result = bookService.getCountOfPages(bookId);

        assertEquals(42, result);
    }

    @Test
    void testGetCountOfPages_zeroPages() {
        var bookId = UUID.randomUUID();
        when(pageService.getCountOfPages(bookId)).thenReturn(0);

        assertEquals(0, bookService.getCountOfPages(bookId));
    }
}
