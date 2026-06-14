package com.bookhub.bookservice.services.impls;

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
    public void createBook(Book book) {
        try {
            bookRepository.saveAndFlush(book);
        } catch (DataIntegrityViolationException e) {
            throw new BookAlreadyExistException(book.getTitle());
        }
    }

    @Override
    @Transactional
    public void updateS3ArchivePath(UUID bookId, String path) {
        var book = bookRepository.getReferenceById(bookId);
        book.setS3ArchivePath(path);
        bookRepository.save(book);
    }

}
