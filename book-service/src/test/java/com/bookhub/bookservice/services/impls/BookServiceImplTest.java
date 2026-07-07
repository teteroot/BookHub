package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.*;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;
import com.bookhub.bookservice.services.BookManagementService;
import com.bookhub.bookservice.services.BookStorageService;
import com.bookhub.bookservice.services.PDFService;
import com.bookhub.bookservice.services.PageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookManagementService bookManagementService;
    @Mock
    private BookStorageService bookStorageService;
    @Mock
    private PDFService pdfService;
    @Mock
    private PageService pageService;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void testLoadForbiddenBookStream() {
        var book = Book.builder()
                .id(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .status(BookStatus.DRAFT)
                .build();
        when(bookManagementService.loadBookByUUID(book.getId())).thenReturn(book);
        when(bookManagementService.loadBookPagesByUUID(book.getId())).thenReturn(Collections.emptyList());
        assertThrows(BookAccessDeniedException.class,
                () -> bookService.loadBookStream(UUID.randomUUID(), book.getId()));
    }

    @Test
    void testSuccessfulLoadBookStream() {
        var book = Book.builder()
                .id(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .status(BookStatus.DRAFT)
                .build();
        when(bookManagementService.loadBookByUUID(book.getId())).thenReturn(book);
        when(bookManagementService.loadBookPagesByUUID(book.getId()))
                .thenReturn(List.of(Page.builder()
                        .id(UUID.randomUUID())
                        .s3FilePath("path")
                        .build()));
        when(bookStorageService.loadPageContent("path")).thenReturn(InputStream.nullInputStream());
        assertDoesNotThrow(() -> bookService.loadBookStream(book.getAuthorId(), book.getId()));
        verify(pdfService).collectBookFromPages(anyList());
    }

    @Test
    void testLoadBookStream_publishedBookAccessibleByAnyone() {
        var book = Book.builder()
                .id(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .status(BookStatus.PUBLISHED)
                .build();
        when(bookManagementService.loadBookByUUID(book.getId())).thenReturn(book);
        when(bookManagementService.loadBookPagesByUUID(book.getId()))
                .thenReturn(List.of(Page.builder()
                        .id(UUID.randomUUID())
                        .s3FilePath("path")
                        .build()));
        when(bookStorageService.loadPageContent("path")).thenReturn(InputStream.nullInputStream());

        assertDoesNotThrow(() -> bookService.loadBookStream(UUID.randomUUID(), book.getId()));
        verify(pdfService).collectBookFromPages(anyList());
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
    void testLoadBookStream_noPages_collectsEmptyList() {
        var book = Book.builder()
                .id(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .status(BookStatus.PUBLISHED)
                .build();
        when(bookManagementService.loadBookByUUID(book.getId())).thenReturn(book);
        when(bookManagementService.loadBookPagesByUUID(book.getId())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> bookService.loadBookStream(UUID.randomUUID(), book.getId()));
        verify(pdfService).collectBookFromPages(Collections.emptyList());
        verifyNoInteractions(bookStorageService);
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

        when(bookManagementService.claimBookForUpload(bookId, authorId)).thenReturn(book);
        when(pageService.getCountOfPages(bookId)).thenReturn(0);
        when(pdfService.loadPagesStreams(any())).thenReturn(new LinkedHashMap<>(Map.of(pageStream, 123L)));
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
        when(pdfService.loadPagesStreams(any())).thenReturn(new LinkedHashMap<>());

        bookService.createBookContent(bookId, authorId, InputStream.nullInputStream());

        verify(bookManagementService).removeAllPages(bookId);
        verify(bookStorageService).removeBookContent(bookId);
        verify(bookManagementService).updateBookStatus(bookId, BookStatus.DRAFT);
    }

    @Test
    void testCreateBookContent_multiplePages_incrementsPageNumberSequentially() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var book = Book.builder().id(bookId).authorId(authorId).build();

        var stream1 = InputStream.nullInputStream();
        var stream2 = InputStream.nullInputStream();
        var orderedPages = new LinkedHashMap<InputStream, Long>();
        orderedPages.put(stream1, 100L);
        orderedPages.put(stream2, 200L);

        when(bookManagementService.claimBookForUpload(bookId, authorId)).thenReturn(book);
        when(pageService.getCountOfPages(bookId)).thenReturn(0);
        when(pdfService.loadPagesStreams(any())).thenReturn(orderedPages);
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

        when(bookManagementService.claimBookForUpload(bookId, authorId)).thenReturn(book);
        when(pageService.getCountOfPages(bookId)).thenReturn(0);
        when(pdfService.loadPagesStreams(any())).thenReturn(new LinkedHashMap<>(Map.of(pageStream, 123L)));
        when(pageService.addNewPageToBook(eq(book), anyInt())).thenReturn(UUID.randomUUID());
        when(bookStorageService.createPageContent(eq(bookId), any(), any(), eq(123L)))
                .thenThrow(new RuntimeException("S3 unavailable"));

        assertThrows(ContentSaveException.class,
                () -> bookService.createBookContent(bookId, authorId, InputStream.nullInputStream()));

        verify(bookManagementService).removeAllPages(bookId);
        verify(bookStorageService).removeBookContent(bookId);
        verify(bookManagementService, never()).updateBookStatus(any(), any());
    }

    @Test
    void testCreateBookContent_compensationItselfFails_stillThrowsContentSaveException() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var book = Book.builder().id(bookId).authorId(authorId).build();
        var pageStream = InputStream.nullInputStream();

        when(bookManagementService.claimBookForUpload(bookId, authorId)).thenReturn(book);
        when(pageService.getCountOfPages(bookId)).thenReturn(0);
        when(pdfService.loadPagesStreams(any())).thenReturn(new LinkedHashMap<>(Map.of(pageStream, 123L)));
        when(pageService.addNewPageToBook(eq(book), anyInt())).thenReturn(UUID.randomUUID());
        when(bookStorageService.createPageContent(eq(bookId), any(), any(), eq(123L)))
                .thenThrow(new RuntimeException("S3 unavailable"));
        doThrow(new RuntimeException("DB also down")).when(bookManagementService).removeAllPages(bookId);

        // компенсация логирует ошибку, но не должна маскировать исходный ContentSaveException
        assertThrows(ContentSaveException.class,
                () -> bookService.createBookContent(bookId, authorId, InputStream.nullInputStream()));

        verify(bookStorageService).removeBookContent(bookId);
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
        when(pdfService.loadPagesStreams(any())).thenReturn(new LinkedHashMap<>());

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