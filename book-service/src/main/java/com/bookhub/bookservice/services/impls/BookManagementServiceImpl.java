package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.BookAlreadyExistException;
import com.bookhub.bookservice.exceptions.extensions.BookNotFoundException;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.repositories.BookRepository;
import com.bookhub.bookservice.services.BookManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void createBook(Book book, UUID authorId) {
        book.setAuthorId(authorId);
        book.setStatus(BookStatus.DRAFT);
        try {
            bookRepository.saveAndFlush(book);
        } catch (DataIntegrityViolationException e) {
            throw new BookAlreadyExistException(book.getTitle());
        }
    }
}
