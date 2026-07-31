package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.exceptions.extensions.PageNotFoundException;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;
import com.bookhub.bookservice.repositories.PageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PageServiceImplTest {

    @Mock
    private PageRepository pageRepository;
    @InjectMocks
    private PageServiceImpl pageService;

    @Test
    void testGetCountOfPages_delegatesToRepository() {
        var bookId = UUID.randomUUID();
        when(pageRepository.countByBook_Id(bookId)).thenReturn(42);

        assertEquals(42, pageService.getCountOfPages(bookId));
    }

    @Test
    void testGetCountOfPages_zeroPages() {
        var bookId = UUID.randomUUID();
        when(pageRepository.countByBook_Id(bookId)).thenReturn(0);

        assertEquals(0, pageService.getCountOfPages(bookId));
    }

    @Test
    void testLoadPageByBookIdAndPageNumber_success() {
        var bookId = UUID.randomUUID();
        var page = Page.builder().id(UUID.randomUUID()).pageNumber(3).build();
        when(pageRepository.findByBook_IdAndPageNumber(bookId, 3)).thenReturn(Optional.of(page));

        assertEquals(page, pageService.loadPageByBookIdAndPageNumber(bookId, 3));
    }

    @Test
    void testLoadPageByBookIdAndPageNumber_notFound() {
        var bookId = UUID.randomUUID();
        when(pageRepository.findByBook_IdAndPageNumber(bookId, 99)).thenReturn(Optional.empty());

        assertThrows(PageNotFoundException.class,
                () -> pageService.loadPageByBookIdAndPageNumber(bookId, 99));
    }

    @Test
    void testUpdatePageFilePath_success() {
        var pageId = UUID.randomUUID();
        var page = Page.builder().id(pageId).s3FilePath(null).build();
        when(pageRepository.findById(pageId)).thenReturn(Optional.of(page));

        pageService.updatePageFilePath(pageId, "new-path");

        assertEquals("new-path", page.getS3FilePath());
        verify(pageRepository).save(page);
    }

    @Test
    void testUpdatePageFilePath_overwritesExistingPath() {
        var pageId = UUID.randomUUID();
        var page = Page.builder().id(pageId).s3FilePath("old-path").build();
        when(pageRepository.findById(pageId)).thenReturn(Optional.of(page));

        pageService.updatePageFilePath(pageId, "new-path");

        assertEquals("new-path", page.getS3FilePath());
        verify(pageRepository).save(page);
    }

    @Test
    void testUpdatePageFilePath_pageNotFound() {
        var pageId = UUID.randomUUID();
        when(pageRepository.findById(pageId)).thenReturn(Optional.empty());

        assertThrows(PageNotFoundException.class,
                () -> pageService.updatePageFilePath(pageId, "path"));
        verify(pageRepository, never()).save(any());
    }

    @Test
    void testAddNewPageToBook_success_firstPage() {
        var book = Book.builder().id(UUID.randomUUID()).pages(new ArrayList<>()).build();

        var pageId = pageService.addNewPageToBook(book, 0);

        assertEquals(1, book.getPages().size());
        var addedPage = book.getPages().getFirst();
        assertEquals(pageId, addedPage.getId());
        assertEquals(0, addedPage.getPageNumber());
        assertEquals(book, addedPage.getBook());
        verify(pageRepository).save(addedPage);
    }

    @Test
    void testAddNewPageToBook_middleIndex_pageNumberOffsetByOne() {
        var book = Book.builder().id(UUID.randomUUID()).pages(new ArrayList<>()).build();

        pageService.addNewPageToBook(book, 4);

        var addedPage = book.getPages().getFirst();
        assertEquals(4, addedPage.getPageNumber());
    }

    @Test
    void testAddNewPageToBook_appendsToExistingPages() {
        var existingPage = Page.builder().id(UUID.randomUUID()).pageNumber(1).build();
        var book = Book.builder()
                .id(UUID.randomUUID())
                .pages(new ArrayList<>(List.of(existingPage)))
                .build();

        pageService.addNewPageToBook(book, 1);

        assertEquals(2, book.getPages().size());
        assertEquals(existingPage, book.getPages().getFirst());
    }

    @Test
    void testAddNewPageToBook_returnsGeneratedPageId() {
        var book = Book.builder().id(UUID.randomUUID()).pages(new ArrayList<>()).build();

        var pageId = pageService.addNewPageToBook(book, 0);
        var savedPage = book.getPages().getFirst();

        assertEquals(savedPage.getId(), pageId);
    }

    @Test
    void testLoadBookPagesSortedByPageNumber_returnsPages() {
        var bookId = UUID.randomUUID();
        var page1 = Page.builder().id(UUID.randomUUID()).pageNumber(1).build();
        var page2 = Page.builder().id(UUID.randomUUID()).pageNumber(2).build();
        when(pageRepository.findAllByBook_IdOrderByPageNumber(bookId)).thenReturn(List.of(page1, page2));

        var result = pageService.loadBookPagesSortedByPageNumber(bookId);

        assertEquals(List.of(page1, page2), result);
    }

    @Test
    void testLoadBookPagesSortedByPageNumber_noPages_returnsEmptyList() {
        var bookId = UUID.randomUUID();
        when(pageRepository.findAllByBook_IdOrderByPageNumber(bookId)).thenReturn(Collections.emptyList());

        assertTrue(pageService.loadBookPagesSortedByPageNumber(bookId).isEmpty());
    }
}