package com.bookhub.bookservice.repositories;

import com.bookhub.bookservice.models.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PageRepository extends JpaRepository<Page, UUID> {
    Integer countByBook_Id(UUID bookId);

    List<Page> findAllByBook_IdOrderByPageNumber(UUID bookId);

    Optional<Page> findByBook_IdAndPageNumber(UUID bookId, Integer pageNumber);
}
