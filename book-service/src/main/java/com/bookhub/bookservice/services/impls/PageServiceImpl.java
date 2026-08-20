package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.dtos.entries.BookPageCountEntry;
import com.bookhub.bookservice.exceptions.extensions.PageConcurrentModificationException;
import com.bookhub.bookservice.exceptions.extensions.PageNotFoundException;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;
import com.bookhub.bookservice.repositories.PageRepository;
import com.bookhub.bookservice.services.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;

    @Override
    public Integer getCountOfPages(UUID bookId) {
        return pageRepository.countByBook_Id(bookId);
    }

    @Override
    public List<BookPageCountEntry> getCountOfPages(List<UUID> bookIds) {
        return pageRepository.countAllByBook_IdIn(bookIds);
    }

    @Override
    public Page loadPageByBookIdAndPageNumber(UUID bookId, Integer pageNumber) {
        return pageRepository.findByBook_IdAndPageNumber(bookId, pageNumber).
                orElseThrow(PageNotFoundException::new);
    }

    @Override
    @Transactional
    public void updatePageFilePath(UUID pageId, String path) {
        var page = pageRepository.findById(pageId)
                .orElseThrow(PageNotFoundException::new);
        page.setS3FilePath(path);
        pageRepository.save(page);
    }

    @Override
    public UUID addNewPageToBook(Book book, int pageNumber) {
        var page = Page.builder()
                .book(book)
                .pageNumber(pageNumber)
                .build();
        book.addPage(page);
        pageRepository.save(page);
        return page.getId();
    }

    @Override
    public List<Page> loadBookPagesSortedByPageNumber(UUID bookId) {
        return pageRepository.findAllByBook_IdOrderByPageNumber(bookId);
    }

    @Override
    public Page claimPageForUpdate(UUID pageId, UUID bookId) {
        var page = pageRepository.findPageByIdAndBook_Id(pageId, bookId)
                .orElseThrow(PageNotFoundException::new);
        page.setUpdatedAt(Instant.now());
        try {
            pageRepository.saveAndFlush(page);
        } catch (ObjectOptimisticLockingFailureException e){
            throw new PageConcurrentModificationException();
        }
        return page;
    }

    @Override
    @Transactional
    public Page claimPageForUploadByNumber(UUID bookId, Integer pageNumber) {
        var page = pageRepository.findByBook_IdAndPageNumber(bookId, pageNumber)
                .orElseThrow(PageNotFoundException::new);
        page.setUpdatedAt(Instant.now());
        try {
            pageRepository.saveAndFlush(page);
        } catch (ObjectOptimisticLockingFailureException e){
            throw new PageConcurrentModificationException();
        }
        return page;
    }

    @Override
    @Transactional
    public void putNewPageToBook(Book book, String pagePath, int pageNumber) {
        pageRepository.shiftAllPagesRightFrom(book.getId(),pageNumber);
        var page = Page.builder()
                .book(book)
                .pageNumber(pageNumber)
                .s3FilePath(pagePath)
                .build();
        pageRepository.save(page);
    }

    @Override
    @Transactional
    public void deleteBookPage(Page page, UUID bookId) {
        pageRepository.shiftAllPagesLeftFrom(bookId,page.getPageNumber());
        pageRepository.delete(page);
    }

    @Override
    @Transactional
    public void swapPages(Page page, Page swappedPage) {
        int tempPageNumber = page.getPageNumber();
        page.setPageNumber(swappedPage.getPageNumber());
        swappedPage.setPageNumber(tempPageNumber);
    }

    @Override
    @Transactional
    public void movePageTo(Page page, UUID bookId, Integer pageNumber) {
        if (page.getPageNumber().equals(pageNumber)) {
            return;
        }
        else if (pageNumber > page.getPageNumber()) {
            pageRepository.shiftPagesLeft(bookId,page.getPageNumber(),pageNumber);
        } else {
            pageRepository.shiftPagesRight(bookId,pageNumber,page.getPageNumber());
        }
        page.setPageNumber(pageNumber);
        pageRepository.save(page);
    }


}
