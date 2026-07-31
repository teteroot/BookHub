package com.bookhub.bookservice.repositories;

import com.bookhub.bookservice.models.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PageRepository extends JpaRepository<Page, UUID> {
    Integer countByBook_Id(UUID bookId);

    List<Page> findAllByBook_IdOrderByPageNumber(UUID bookId);

    Optional<Page> findByBook_IdAndPageNumber(UUID bookId, Integer pageNumber);

    Optional<Page> findPageByIdAndBook_Id(UUID id, UUID bookId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Page p
            SET p.pageNumber = p.pageNumber + 1
            WHERE p.book.id = :bookId
            AND p.pageNumber >= :from
            AND p.pageNumber <= :to
           """)
    void shiftPagesRight(@Param("bookId") UUID bookId, @Param("from") int from, @Param("to") int to);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Page p
            SET p.pageNumber = p.pageNumber + 1
            WHERE p.book.id = :bookId AND p.pageNumber >= :from
           """)
    void shiftAllPagesRightFrom(@Param("bookId") UUID bookId, @Param("from") int from);


    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Page p
            SET p.pageNumber = p.pageNumber + -1
            WHERE p.book.id = :bookId AND p.pageNumber >= :from
           """)
    void shiftAllPagesLeftFrom(@Param("bookId") UUID bookId, @Param("from") int from);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Page p
            SET p.pageNumber = p.pageNumber + -1
            WHERE p.book.id = :bookId
            AND p.pageNumber >= :from
            AND p.pageNumber <= :to
           """)
    void shiftPagesLeft(@Param("bookId") UUID bookId, @Param("from") int from, @Param("to") int to);
}
