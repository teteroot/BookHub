package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.*;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.repositories.BookRepository;
import com.bookhub.bookservice.services.BookManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
    public Book loadAuthorBookByUUID(UUID bookId, UUID authorId) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);
        if (!book.getAuthorId().equals(authorId)){
            throw new BookAccessDeniedException();
        }
        return book;
    }

    @Override
    @Transactional
    public Book claimBookForUpdate(UUID bookId, UUID authorId) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);
        if (!book.getAuthorId().equals(authorId)){
            throw new BookAccessDeniedException();
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

    @Override
    @Transactional
    public void removeContentPath(UUID bookId) {
        var book = bookRepository.getReferenceById(bookId);
        book.setS3ArchivePath(null);
    }

    @Override
    @Transactional
    public void updateBookCoverPath(UUID bookId, String coverPath) {
        var book = bookRepository.getReferenceById(bookId);
        book.setS3CoverPath(coverPath);
    }

    @Override
    public void deleteBookByUUID(UUID bookId) {
        bookRepository.deleteById(bookId);
    }

    @Override
    public boolean isExistAndPublishedBook(UUID bookId) {
        return bookRepository.existsByIdAndStatus(bookId,BookStatus.PUBLISHED);
    }

    @Override
    @Transactional
    public void incrementBookStars(UUID bookId, int weight) {
        if (bookRepository.incrementStars(bookId, weight) == 0){
            throw new BookNotFoundException();
        }
    }

    @Override
    public List<Book> loadPublishedBooksByIds(List<UUID> bookIds, UUID authorId) {
        var books = bookRepository.findAllByIdsAndStatusOrIdAndAuthorId(bookIds, BookStatus.PUBLISHED, authorId);
        if (books.isEmpty()) throw new BookNotFoundException();
        return books;
    }

    @Override
    public Page<Book> loadBooksWithAuthorIdAndStatus(UUID authorId,BookStatus status, Integer page) {
        return bookRepository.findAllByAuthorIdAndStatus(authorId,status, PageRequest.of(page, 10));
    }

}
