package com.bookhub.bookservice.orchestrators.impls;

import com.bookhub.bookservice.dtos.entries.BookPageCountEntry;
import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.BookNotDraftingException;
import com.bookhub.bookservice.exceptions.extensions.InvalidPageNumberException;
import com.bookhub.bookservice.orchestrators.BookPageOrchestrator;
import com.bookhub.bookservice.services.BookManagementService;
import com.bookhub.bookservice.services.PageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookPageOrchestratorImpl implements BookPageOrchestrator {

    private final BookManagementService bookManagementService;
    private final PageService pageService;

    @Override
    public Integer getCountOfPages(UUID bookId) {
        return pageService.getCountOfPages(bookId);
    }

    @Override
    public Map<UUID,Integer> getAllCountOfPages(List<UUID> bookIds) {
        return pageService.getCountOfPages(bookIds).stream()
                .collect(
                        Collectors.toMap(BookPageCountEntry::bookId,
                                b -> b.count().intValue())
                );
    }

    @Override
    @Transactional
    public void swapBookPages(UUID bookId, UUID authorId, UUID pageId, UUID swapPageId) {
        var book = bookManagementService.claimBookForUpdate(bookId ,authorId);
        if (!book.getStatus().equals(BookStatus.DRAFT)){
            throw new BookNotDraftingException();
        }
        var page = pageService.claimPageForUpdate(pageId,bookId);
        var swappedPage = pageService.claimPageForUpdate(swapPageId,bookId);
        bookManagementService.removeContentPath(bookId);
        pageService.swapPages(page,swappedPage);
    }

    @Override
    @Transactional
    public void moveBookPage(UUID bookId, UUID authorId, UUID pageId, Integer pageNumber) {
        var book = bookManagementService.claimBookForUpdate(bookId ,authorId);
        if (!book.getStatus().equals(BookStatus.DRAFT)){
            throw new BookNotDraftingException();
        }
        if (pageNumber < 1 || pageNumber > pageService.getCountOfPages(bookId)){
            throw new InvalidPageNumberException();
        }
        var page = pageService.claimPageForUpdate(pageId,bookId);
        bookManagementService.removeContentPath(bookId);
        pageService.movePageTo(page,bookId, pageNumber);
    }
}
