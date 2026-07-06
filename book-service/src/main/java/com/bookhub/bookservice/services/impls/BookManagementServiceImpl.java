package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.BookAlreadyExistException;
import com.bookhub.bookservice.exceptions.extensions.BookNotFoundException;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.models.Page;
import com.bookhub.bookservice.repositories.BookRepository;
import com.bookhub.bookservice.services.BookManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BookManagementServiceImpl implements BookManagementService {

    private final BookRepository bookRepository;

    @Override
    public Book loadBookByUUID(UUID uuid) {
        return bookRepository.findById(uuid)
                .orElseThrow(BookNotFoundException::new);
    }

    @Override
    @Transactional
    public void createBook(Book book) {
        try {
            bookRepository.saveAndFlush(book);
        } catch (DataIntegrityViolationException e) {
            throw new BookAlreadyExistException(book.getTitle());
        }
    }

    @Override
    public List<Page> loadBookPagesByUUID(UUID bookId) {
        var book = bookRepository.getReferenceById(bookId);
        return book.getPages();
    }

    @Override
    @Transactional
    public void removeAllPages(UUID bookId) {
        var book = bookRepository.getReferenceById(bookId);
        book.getPages().clear();
    }

    @Override
    @Transactional
    public void updateBookStatus(UUID bookId, BookStatus bookStatus) {
        var book = bookRepository.getReferenceById(bookId);
        book.setStatus(bookStatus);
    }

}
