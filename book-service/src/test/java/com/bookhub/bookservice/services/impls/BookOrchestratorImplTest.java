package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.*;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;
import com.bookhub.bookservice.services.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    @Mock
    private ImageService imageService;

    @InjectMocks
    private BookOrchestratorImpl orchestrator;

    @Test
    void loadBookStream_deniesUnpublishedBookToNonAuthor() {
        var book = book(BookStatus.DRAFT);
        when(bookManagementService.loadBookByUUID(book.getId())).thenReturn(book);

        assertThrows(BookAccessDeniedException.class,
                () -> orchestrator.loadBookStream(book.getId(), UUID.randomUUID()));
        verifyNoInteractions(bookStorageService, pageService, pdfService);
    }

    @Test
    void loadBookStream_returnsCachedArchive() {
        var book = book(BookStatus.DRAFT);
        book.setS3ArchivePath("archive");
        var expected = InputStream.nullInputStream();
        when(bookManagementService.loadBookByUUID(book.getId())).thenReturn(book);
        when(bookStorageService.loadContent("archive")).thenReturn(expected);

        assertSame(expected, orchestrator.loadBookStream(book.getId(), book.getAuthorId()));
        verify(bookStorageService).loadContent("archive");
        verifyNoInteractions(pdfService, fileTempService);
    }

    @Test
    void loadBookStream_cacheMiss_createsAndStoresArchive() {
        var book = book(BookStatus.DRAFT);
        var page = Page.builder().id(UUID.randomUUID()).s3FilePath("page").build();
        var pageFile = Path.of("page.pdf");
        var bookFile = Path.of("book.pdf");
        when(bookManagementService.loadBookByUUID(book.getId())).thenReturn(book);
        when(pageService.loadBookPagesSortedByPageNumber(book.getId())).thenReturn(List.of(page));
        when(fileTempService.writeToTempFile(eq("page-"), eq(".pdf"), any())).thenReturn(pageFile);
        when(pdfService.collectBookFromPages(List.of(pageFile))).thenReturn(bookFile);
        when(fileTempService.openStream(bookFile)).thenReturn(InputStream.nullInputStream());
        when(fileTempService.sizeOf(bookFile)).thenReturn(12L);
        when(bookStorageService.createBookContent(eq(book.getId()), any(), eq(12L))).thenReturn("archive");
        when(bookStorageService.loadContent("archive")).thenReturn(InputStream.nullInputStream());

        orchestrator.loadBookStream(book.getId(), book.getAuthorId());

        verify(bookManagementService).updateBookContentPath(book.getId(), "archive");
        verify(fileTempService).deleteQuietly(argThat(paths -> paths.contains(pageFile) && paths.contains(bookFile)));
    }

    @Test
    void loadBookStream_cacheMiss_withoutPagesThrowsNotFound() {
        var book = book(BookStatus.PUBLISHED);
        when(bookManagementService.loadBookByUUID(book.getId())).thenReturn(book);
        when(pageService.loadBookPagesSortedByPageNumber(book.getId())).thenReturn(List.of());

        assertThrows(BookContentNotFoundException.class,
                () -> orchestrator.loadBookStream(book.getId(), UUID.randomUUID()));
    }

    @Test
    void loadPageStream_checksAccessAndReturnsPageContent() {
        var book = book(BookStatus.PUBLISHED);
        var pageId = UUID.randomUUID();
        var page = Page.builder().id(pageId).s3FilePath("page").build();
        var expected = InputStream.nullInputStream();
        when(bookManagementService.loadBookByUUID(book.getId())).thenReturn(book);
        when(pageService.loadPageByBookIdAndPageNumber(book.getId(), 2)).thenReturn(page);
        when(bookStorageService.loadContent("page")).thenReturn(expected);

        var result = orchestrator.loadPageStreamByBookIdAndPageNumber(book.getId(), UUID.randomUUID(), 2);

        assertSame(expected, result.stream());
        assertEquals(pageId, result.pageId());
    }

    @Test
    void loadPageStream_deniesUnpublishedBookToNonAuthor() {
        var book = book(BookStatus.ARCHIVED);
        when(bookManagementService.loadBookByUUID(book.getId())).thenReturn(book);

        assertThrows(BookAccessDeniedException.class,
                () -> orchestrator.loadPageStreamByBookIdAndPageNumber(book.getId(), UUID.randomUUID(), 1));
        verifyNoInteractions(pageService, bookStorageService);
    }

    @Test
    void loadBookByUUID_allowsAuthorAndPublishedReader() {
        var draft = book(BookStatus.DRAFT);
        when(bookManagementService.loadBookByUUID(draft.getId())).thenReturn(draft);
        assertSame(draft, orchestrator.loadBookByUUID(draft.getId(), draft.getAuthorId()));

        var published = book(BookStatus.PUBLISHED);
        when(bookManagementService.loadBookByUUID(published.getId())).thenReturn(published);
        assertSame(published, orchestrator.loadBookByUUID(published.getId(), UUID.randomUUID()));
    }

    @Test
    void loadBookByUUID_deniesUnpublishedBookToNonAuthor() {
        var book = book(BookStatus.ARCHIVED);
        when(bookManagementService.loadBookByUUID(book.getId())).thenReturn(book);

        assertThrows(BookAccessDeniedException.class,
                () -> orchestrator.loadBookByUUID(book.getId(), UUID.randomUUID()));
    }

    @Test
    void createBook_setsAuthorAndEmptyStatus() {
        var book = Book.builder().build();
        var authorId = UUID.randomUUID();

        orchestrator.createBook(book, authorId);

        assertEquals(authorId, book.getAuthorId());
        assertEquals(BookStatus.EMPTY, book.getStatus());
        verify(bookManagementService).createBook(book);
    }

    @Test
    void createBookContent_uploadsPagesAndChangesStatus() {
        var book = book(BookStatus.EMPTY);
        var files = List.of(Path.of("one.pdf"), Path.of("two.pdf"));
        when(bookManagementService.claimBookForUpdate(book.getId(), book.getAuthorId())).thenReturn(book);
        when(pageService.getCountOfPages(book.getId())).thenReturn(0);
        when(pdfService.loadPages(any())).thenReturn(files);
        when(fileTempService.openStream(any())).thenReturn(InputStream.nullInputStream());
        when(fileTempService.sizeOf(files.get(0))).thenReturn(10L);
        when(fileTempService.sizeOf(files.get(1))).thenReturn(20L);
        when(pageService.addNewPageToBook(eq(book), anyInt())).thenReturn(UUID.randomUUID());
        when(bookStorageService.createPageContent(eq(book.getId()), any(), anyLong())).thenReturn("page-path");

        orchestrator.createBookContent(book.getId(), book.getAuthorId(), InputStream.nullInputStream());

        var numbers = ArgumentCaptor.forClass(Integer.class);
        verify(pageService, times(2)).addNewPageToBook(eq(book), numbers.capture());
        assertEquals(List.of(1, 2), numbers.getAllValues());
        verify(bookManagementService).updateBookStatus(book.getId(), BookStatus.DRAFT);
        verify(fileTempService).deleteQuietly(files);
    }

    @Test
    void createBookContent_replacesExistingPages() {
        var book = book(BookStatus.EMPTY);
        when(bookManagementService.claimBookForUpdate(book.getId(), book.getAuthorId())).thenReturn(book);
        when(pageService.getCountOfPages(book.getId())).thenReturn(1);
        when(pdfService.loadPages(any())).thenReturn(List.of());

        orchestrator.createBookContent(book.getId(), book.getAuthorId(), InputStream.nullInputStream());

        verify(bookManagementService).removeAllPages(book.getId());
        verify(bookStorageService).removeBook(book.getId());
        verify(bookManagementService).updateBookStatus(book.getId(), BookStatus.DRAFT);
    }

    @Test
    void createBookContent_rejectsContentForNonEmptyBook() {
        var book = book(BookStatus.DRAFT);
        when(bookManagementService.claimBookForUpdate(book.getId(), book.getAuthorId())).thenReturn(book);

        assertThrows(BookContentAlreadyExistException.class,
                () -> orchestrator.createBookContent(book.getId(), book.getAuthorId(), InputStream.nullInputStream()));
        verifyNoInteractions(pdfService, bookStorageService);
    }

    @Test
    void createBookContent_failureCompensatesAndWrapsException() {
        var book = book(BookStatus.EMPTY);
        var file = Path.of("page.pdf");
        when(bookManagementService.claimBookForUpdate(book.getId(), book.getAuthorId())).thenReturn(book);
        when(pageService.getCountOfPages(book.getId())).thenReturn(0);
        when(pdfService.loadPages(any())).thenReturn(List.of(file));
        when(fileTempService.openStream(file)).thenReturn(InputStream.nullInputStream());
        when(fileTempService.sizeOf(file)).thenReturn(10L);
        when(pageService.addNewPageToBook(eq(book), anyInt())).thenReturn(UUID.randomUUID());
        when(bookStorageService.createPageContent(eq(book.getId()), any(), eq(10L)))
                .thenThrow(new RuntimeException("storage failure"));

        assertThrows(ContentSaveException.class,
                () -> orchestrator.createBookContent(book.getId(), book.getAuthorId(), InputStream.nullInputStream()));
        verify(bookManagementService).removeAllPages(book.getId());
        verify(bookStorageService).removeBook(book.getId());
        verify(bookManagementService, never()).updateBookStatus(any(), any());
    }

    @Test
    void updatePageContent_updatesOnePageAndRemovesCachedArchive() {
        var book = book(BookStatus.DRAFT);
        var page = Page.builder().s3FilePath("old-page").build();
        var file = Path.of("page.pdf");
        when(bookManagementService.loadAuthorBookByUUID(book.getId(), book.getAuthorId())).thenReturn(book);
        when(pageService.claimPageForUpload(page.getId(), book.getId())).thenReturn(page);
        when(pdfService.loadPages(any())).thenReturn(List.of(file));
        when(fileTempService.openStream(file)).thenReturn(InputStream.nullInputStream());
        when(fileTempService.sizeOf(file)).thenReturn(7L);

        orchestrator.updatePageContent(book.getId(), book.getAuthorId(), page.getId(), InputStream.nullInputStream());

        verify(bookManagementService).removeContentPath(book.getId());
        verify(bookStorageService).updatePageContent(eq("old-page"), any(), eq(7L));
        verify(fileTempService).deleteQuietly(List.of(file));
    }

    @Test
    void updatePageContent_rejectsMultiplePages() {
        var book = book(BookStatus.DRAFT);
        when(bookManagementService.loadAuthorBookByUUID(book.getId(), book.getAuthorId())).thenReturn(book);
        when(pageService.claimPageForUpload(any(), eq(book.getId()))).thenReturn(Page.builder().build());
        when(pdfService.loadPages(any())).thenReturn(List.of(Path.of("1.pdf"), Path.of("2.pdf")));

        assertThrows(TooManyPagesException.class,
                () -> orchestrator.updatePageContent(book.getId(), book.getAuthorId(), UUID.randomUUID(), InputStream.nullInputStream()));
        verify(bookManagementService, never()).removeContentPath(any());
    }

    @Test
    void createBookPage_usesNextNumberWhenNumberOmitted() {
        var book = book(BookStatus.DRAFT);
        var file = Path.of("page.pdf");
        when(bookManagementService.claimBookForUpdate(book.getId(), book.getAuthorId())).thenReturn(book);
        when(pageService.getCountOfPages(book.getId())).thenReturn(3);
        when(pdfService.loadPages(any())).thenReturn(List.of(file));
        when(fileTempService.openStream(file)).thenReturn(InputStream.nullInputStream());
        when(fileTempService.sizeOf(file)).thenReturn(9L);
        when(bookStorageService.createPageContent(eq(book.getId()), any(), eq(9L))).thenReturn("new-page");

        orchestrator.createBookPage(book.getId(), book.getAuthorId(), null, InputStream.nullInputStream());

        verify(bookManagementService).removeContentPath(book.getId());
        verify(pageService).putNewPageToBook(book, "new-page", 4);
    }

    @Test
    void createBookPage_rejectsMultiplePagesAndDoesNotStoreContent() {
        var book = book(BookStatus.DRAFT);
        when(bookManagementService.claimBookForUpdate(book.getId(), book.getAuthorId())).thenReturn(book);
        when(pdfService.loadPages(any())).thenReturn(List.of(Path.of("1.pdf"), Path.of("2.pdf")));

        assertThrows(TooManyPagesException.class,
                () -> orchestrator.createBookPage(book.getId(), book.getAuthorId(), 1, InputStream.nullInputStream()));
        verify(bookStorageService, never()).createPageContent(any(), any(), anyLong());
    }

    @Test
    void updateBookCover_storesCommonFormatAndRemovesOldCover() {
        var book = book(BookStatus.DRAFT);
        book.setS3CoverPath("old-cover");
        var type = MediaType.IMAGE_JPEG;
        when(bookManagementService.claimBookForUpdate(book.getId(), book.getAuthorId())).thenReturn(book);
        when(imageService.getCommonCoverType()).thenReturn(type);
        when(bookStorageService.createBookCover(eq(book.getId()), eq(".jpeg"), eq(type.toString()), any(), eq(5L)))
                .thenReturn("new-cover");

        orchestrator.updateBookCover(book.getId(), book.getAuthorId(), InputStream.nullInputStream(), 5L, type);

        verify(bookManagementService).updateBookCoverPath(book.getId(), "new-cover");
        verify(bookStorageService).removeBookStorageContent("old-cover");
    }

    @Test
    void updateBookCover_removesNewStorageWhenDatabaseUpdateFails() {
        var book = book(BookStatus.DRAFT);
        var type = MediaType.IMAGE_JPEG;
        when(bookManagementService.claimBookForUpdate(book.getId(), book.getAuthorId())).thenReturn(book);
        when(imageService.getCommonCoverType()).thenReturn(type);
        when(bookStorageService.createBookCover(any(), anyString(), anyString(), any(), anyLong())).thenReturn("new-cover");
        doThrow(new DataAccessException("database failure") {}).when(bookManagementService)
                .updateBookCoverPath(book.getId(), "new-cover");

        assertThrows(CoverSaveException.class,
                () -> orchestrator.updateBookCover(book.getId(), book.getAuthorId(), InputStream.nullInputStream(), 5L, type));
        verify(bookStorageService).removeBookStorageContent("new-cover");
    }

    @Test
    void loadBookCoverStream_returnsCachedCover() {
        var book = book(BookStatus.PUBLISHED);
        book.setS3CoverPath("cover");
        var expected = InputStream.nullInputStream();
        when(bookManagementService.loadBookByUUID(book.getId())).thenReturn(book);
        when(bookStorageService.loadContent("cover")).thenReturn(expected);

        assertSame(expected, orchestrator.loadBookCoverStream(book.getId(), UUID.randomUUID()));
        verifyNoInteractions(pageService, pdfService, fileTempService);
    }

    @Test
    void loadBookCoverStream_withoutCoverCachesImageFromFirstPage() {
        var book = book(BookStatus.PUBLISHED);
        var type = MediaType.IMAGE_JPEG;
        var pageFile = Path.of("page.pdf");
        var imageFile = Path.of("cover.jpg");
        when(bookManagementService.loadBookByUUID(book.getId())).thenReturn(book);
        when(imageService.getCommonCoverType()).thenReturn(type);
        when(pageService.claimPageForUploadByNumber(book.getId(), 1)).thenReturn(Page.builder().s3FilePath("page").build());
        when(fileTempService.writeToTempFile(eq("page-"), eq(".pdf"), any())).thenReturn(pageFile);
        when(pdfService.convertSinglePageToImage(pageFile, type, 150)).thenReturn(imageFile);
        when(fileTempService.openStream(imageFile)).thenReturn(InputStream.nullInputStream());
        when(fileTempService.sizeOf(imageFile)).thenReturn(20L);
        when(bookStorageService.createBookCover(eq(book.getId()), eq(".jpeg"), eq(type.toString()), any(), eq(20L)))
                .thenReturn("cover");
        when(bookStorageService.loadContent("cover")).thenReturn(InputStream.nullInputStream());
        when(imageService.getPdfDpi()).thenReturn(150);

        orchestrator.loadBookCoverStream(book.getId(), UUID.randomUUID());

        verify(bookManagementService).updateBookCoverPath(book.getId(), "cover");
        verify(fileTempService).deleteQuietly(List.of(pageFile, imageFile));
    }

    @Test
    void loadBookCoverContentType_delegatesToImageService() {
        when(imageService.getCommonCoverType()).thenReturn(MediaType.IMAGE_PNG);
        assertEquals(MediaType.IMAGE_PNG, orchestrator.loadBookCoverContentType());
    }

    @Test
    void publishBook_requiresDraftWithPagesAndPublishes() {
        var book = book(BookStatus.DRAFT);
        book.setS3ArchivePath("archive");
        book.setS3CoverPath("cover");
        when(bookManagementService.loadAuthorBookByUUID(book.getId(), book.getAuthorId())).thenReturn(book);
        when(pageService.getCountOfPages(book.getId())).thenReturn(2);

        orchestrator.publishBook(book.getId(), book.getAuthorId());

        verify(bookManagementService).updateBookStatus(book.getId(), BookStatus.PUBLISHED);
    }

    @Test
    void publishBook_rejectsEmptyContent() {
        var book = book(BookStatus.DRAFT);
        when(bookManagementService.loadAuthorBookByUUID(book.getId(), book.getAuthorId())).thenReturn(book);
        when(pageService.getCountOfPages(book.getId())).thenReturn(0);

        assertThrows(BookContentNotFoundException.class,
                () -> orchestrator.publishBook(book.getId(), book.getAuthorId()));
        verify(bookManagementService, never()).updateBookStatus(any(), any());
    }

    @Test
    void publishBook_rejectsNonDraft() {
        var book = book(BookStatus.PUBLISHED);
        when(bookManagementService.loadAuthorBookByUUID(book.getId(), book.getAuthorId())).thenReturn(book);

        assertThrows(BookNotDraftingException.class,
                () -> orchestrator.publishBook(book.getId(), book.getAuthorId()));
        verifyNoInteractions(pageService);
    }

    @Test
    void draftBook_validatesStatusAndUpdates() {
        var published = book(BookStatus.PUBLISHED);
        when(bookManagementService.loadAuthorBookByUUID(published.getId(), published.getAuthorId())).thenReturn(published);
        orchestrator.draftBook(published.getId(), published.getAuthorId());
        verify(bookManagementService).updateBookStatus(published.getId(), BookStatus.DRAFT);

        var draft = book(BookStatus.DRAFT);
        when(bookManagementService.loadAuthorBookByUUID(draft.getId(), draft.getAuthorId())).thenReturn(draft);
        assertThrows(BookAlreadyRequireStatusException.class,
                () -> orchestrator.draftBook(draft.getId(), draft.getAuthorId()));
    }

    @Test
    void archiveBook_rejectsEmptyAndArchivesPublishedBook() {
        var published = book(BookStatus.PUBLISHED);
        when(bookManagementService.loadAuthorBookByUUID(published.getId(), published.getAuthorId())).thenReturn(published);
        orchestrator.archiveBook(published.getId(), published.getAuthorId());
        verify(bookManagementService).updateBookStatus(published.getId(), BookStatus.ARCHIVED);

        var empty = book(BookStatus.EMPTY);
        when(bookManagementService.loadAuthorBookByUUID(empty.getId(), empty.getAuthorId())).thenReturn(empty);
        assertThrows(BookContentNotFoundException.class,
                () -> orchestrator.archiveBook(empty.getId(), empty.getAuthorId()));
    }

    @Test
    void deleteBook_deletesDatabaseRecordAndStorage() {
        var bookId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        when(bookManagementService.loadAuthorBookByUUID(bookId, authorId)).thenReturn(book(BookStatus.DRAFT));

        orchestrator.deleteBook(bookId, authorId);

        verify(bookManagementService).deleteBookByUUID(bookId);
        verify(bookStorageService).removeBook(bookId);
    }

    @Test
    void pageQueries_delegateToPageService() {
        var bookId = UUID.randomUUID();
        var page = Page.builder().id(UUID.randomUUID()).build();
        when(pageService.loadPageByBookIdAndPageNumber(bookId, 4)).thenReturn(page);
        when(pageService.getCountOfPages(bookId)).thenReturn(4);

        assertSame(page, orchestrator.loadPageByBookIdAndPageNumber(bookId, 4));
        assertEquals(4, orchestrator.getCountOfPages(bookId));
    }

    private static Book book(BookStatus status) {
        return Book.builder().id(UUID.randomUUID()).authorId(UUID.randomUUID()).status(status).build();
    }
}
