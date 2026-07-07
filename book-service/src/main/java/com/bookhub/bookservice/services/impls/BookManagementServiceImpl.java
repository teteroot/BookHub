package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.*;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.repositories.BookRepository;
import com.bookhub.bookservice.services.BookManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
    public Book claimBookForUpload(UUID bookId, UUID authorId) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);
        if (!book.getAuthorId().equals(authorId)){
            throw new BookAccessDeniedException();
        }
        if (!book.getStatus().equals(BookStatus.EMPTY)) {
            throw new BookContentAlreadyExistException();
        }
        book.setUpdatedAt(Instant.now());
        try {
            bookRepository.saveAndFlush(book);
        } catch (ObjectOptimisticLockingFailureException e){
            throw new BookConcurrentModificationException();
        }
        return book;
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

    @Override
    @Transactional
    public void updateBookContentPath(UUID bookId,String path) {
        var book = bookRepository.getReferenceById(bookId);
        book.setS3ArchivePath(path);
    }

}
