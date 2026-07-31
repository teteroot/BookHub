package com.bookhub.bookservice.repositories;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    boolean existsByIdAndStatus(UUID id, BookStatus status);

    @Modifying
    @Query("""
            UPDATE Book b
            SET b.countOfStars = b.countOfStars + :weight
            WHERE b.id = :bookId
            """)
    int incrementStars(UUID bookId, int weight);
}
