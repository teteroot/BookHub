package com.bookhub.bookservice.orchestrators.impls;

import com.bookhub.bookservice.dtos.entries.BookPageCountEntry;
import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.BookNotDraftingException;
import com.bookhub.bookservice.exceptions.extensions.InvalidPageNumberException;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.services.BookManagementService;
import com.bookhub.bookservice.services.PageService;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookPageOrchestratorImplTest {

    @Mock
    private BookManagementService bookManagementService;
    @Mock
    private PageService pageService;

    @InjectMocks
    private BookPageOrchestratorImpl bookPageOrchestrator;


    @Test
    void testGetCountOfPages() {
        when(pageService.getCountOfPages(any(UUID.class))).thenReturn(1);
        assertEquals(1,bookPageOrchestrator.getCountOfPages(UUID.randomUUID()));
    }

    @RepeatedTest(100)
    void testGetAllCountOfPages() {
        var pages = List.of(UUID.randomUUID(), UUID.randomUUID());
        var counts = List.of(
                new BookPageCountEntry(pages.get(0), 1L),
                new BookPageCountEntry(pages.get(1),2L));
        when(pageService.getCountOfPages(pages))
                .thenReturn(counts);
        assertThat(bookPageOrchestrator.getAllCountOfPages(pages).values()).containsExactlyInAnyOrder(1,2);
    }

    @Test
    void testSwapNonDraftedBookPages() {
        var book = book(BookStatus.PUBLISHED);
        when(bookManagementService.claimBookForUpdate(any(UUID.class), any(UUID.class))).thenReturn(book);
        assertThrows(BookNotDraftingException.class, () -> bookPageOrchestrator.swapBookPages(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        ));
    }

    @Test
    void testSuccessfulSwapBookPages() {
        var book = book(BookStatus.DRAFT);
        var pageId1 =  UUID.randomUUID();
        var pageId2 =  UUID.randomUUID();
        when(bookManagementService.claimBookForUpdate(any(UUID.class), any(UUID.class))).thenReturn(book);
        assertDoesNotThrow(() -> bookPageOrchestrator.swapBookPages(
                book.getId(),
                UUID.randomUUID(),
                pageId1,
                pageId2
                ));
        verify(bookManagementService).removeContentPath(book.getId());
        verify(pageService).swapPages(any(),any());
    }

    @Test
    void testMoveNonDraftedBookPage() {
        var book = book(BookStatus.PUBLISHED);
        when(bookManagementService.claimBookForUpdate(any(UUID.class), any(UUID.class))).thenReturn(book);
        assertThrows(BookNotDraftingException.class, () -> bookPageOrchestrator.moveBookPage(
                book.getId(),UUID.randomUUID(),UUID.randomUUID(), 3)
        );
    }

    @Test
    void testMoveBookIncorrectPage() {
        var book = book(BookStatus.DRAFT);
        when(bookManagementService.claimBookForUpdate(any(UUID.class), any(UUID.class))).thenReturn(book);
        assertThrows(InvalidPageNumberException.class, () -> bookPageOrchestrator.moveBookPage(
                book.getId(),UUID.randomUUID(),UUID.randomUUID(), -1)
        );
    }

    @Test
    void testSuccessfulMoveBookPage() {
        var book = book(BookStatus.DRAFT);
        when(bookManagementService.claimBookForUpdate(any(UUID.class), any(UUID.class))).thenReturn(book);
        when(pageService.getCountOfPages(book.getId())).thenReturn(5);
        assertDoesNotThrow(() -> bookPageOrchestrator.moveBookPage(
                book.getId(),UUID.randomUUID(),UUID.randomUUID(), 1)
        );
        verify(bookManagementService).removeContentPath(book.getId());
        verify(pageService).movePageTo(any(), any(), anyInt());
    }

    private static Book book(BookStatus status) {
        return Book.builder().id(UUID.randomUUID()).authorId(UUID.randomUUID()).status(status).build();
    }
}
