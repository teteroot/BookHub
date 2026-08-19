package com.bookhub.bookservice.repositories;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.models.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query("""
        SELECT b
        FROM Book b
        WHERE b.id IN :ids
        AND (b.status = :status OR b.authorId = :authorId)
        """)
    List<Book> findAllByIdsAndStatusOrIdAndAuthorId(@Param("ids") List<UUID> ids, @Param("status") BookStatus status, @Param("authorId") UUID authorId);

    @Query("""
           SELECT b
           FROM Book b
           WHERE (:authorId IS NULL OR b.authorId = :authorId)
           AND (:status IS NULL OR b.status = :status)
           """)
    Page<Book> findAllByAuthorIdAndStatus(UUID authorId, BookStatus status, Pageable pageable);
}
