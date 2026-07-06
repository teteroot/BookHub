package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.exceptions.extensions.PageNotFoundException;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;
import com.bookhub.bookservice.repositories.PageRepository;
import com.bookhub.bookservice.services.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public UUID addNewPageToBook(Book book, int index) {
        var page = Page.builder()
                .book(book)
                .pageNumber(index+1)
                .originalPageIndex(index)
                .build();
        book.addPage(page);
        pageRepository.save(page);
        return page.getId();
    }


}
