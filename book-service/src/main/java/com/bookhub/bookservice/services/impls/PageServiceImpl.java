package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.exceptions.extensions.PageNotFoundException;
import com.bookhub.bookservice.models.Page;
import com.bookhub.bookservice.repositories.PageRepository;
import com.bookhub.bookservice.services.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public List<Page> loadPagesByBookId(UUID bookId) {
        return pageRepository.findAllByBook_IdOrderByPageNumber(bookId);
    }

    @Override
    public Page loadPageByBookIdAndPageNumber(UUID bookId, Integer pageNumber) {
        return pageRepository.findByBook_IdAndPageNumber(bookId, pageNumber).
                orElseThrow(PageNotFoundException::new);
    }


}
