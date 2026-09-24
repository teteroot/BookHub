package com.bookhub.profileservice.repositories;

import com.bookhub.profileservice.models.FavoriteBook;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FavoriteBookRepository extends CrudRepository<FavoriteBook, UUID> {

    boolean existsByPersonIdAndBookId(UUID personId, UUID bookId);
    List<FavoriteBook> findAllByPersonIdOrderByCreatedAtDesc(UUID personId);

    int deleteByPersonIdAndBookId(UUID personId, UUID bookId);

    void deleteAllByBookId(UUID bookId);
}
