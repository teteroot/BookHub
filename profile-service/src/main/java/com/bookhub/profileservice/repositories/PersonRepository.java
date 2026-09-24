package com.bookhub.profileservice.repositories;

import com.bookhub.profileservice.dtos.responses.BiographyResponseDto;
import com.bookhub.profileservice.enums.UserRole;
import com.bookhub.profileservice.models.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonRepository extends CrudRepository<Person, UUID> {
    Optional<BiographyResponseDto> findBiographyById(UUID id);

    @Query("""
           SELECT p
           FROM Person p
           WHERE (LOWER(p.firstName) LIKE LOWER(CONCAT(:firstName, "%")) AND LOWER(p.lastName) LIKE LOWER(CONCAT(:lastName, "%")) )
           OR (LOWER(p.lastName) LIKE LOWER(CONCAT(:firstName, "%")) AND (LOWER(p.firstName)) LIKE LOWER(CONCAT(:lastName, "%")))
           """)
    Page<Person> findByFirstNameAndLastNameOrLastNameAndFirstName(String firstName, String lastName, Pageable pageable);
    @Query("""
           SELECT p
           FROM Person p
           WHERE LOWER(p.firstName) LIKE LOWER(CONCAT(:name, "%"))
           OR LOWER(p.lastName) LIKE LOWER(CONCAT(:name, "%"))
           """)
    Page<Person> findByFirstNameOrLastName(String name,
                                           Pageable pageable);

    @Query("""
           SELECT p
           FROM Person p
           WHERE p.role=:role
           ORDER BY RANDOM() DESC
           """)
    Page<Person> findPersonByRoleGroupByMarkedAsFavoriteAuthorsIdSize(UserRole role,
                                                                      Pageable pageable);

    @Modifying
    @Query(value = """
          INSERT
          INTO favorite_authors(marked_as_favorite_by_id,favorite_author_id)
          VALUES (:id,:authorId);
          """, nativeQuery = true)
    void addPersonToFavoriteAuthors(UUID id, UUID authorId);

    @Modifying
    @Query(value = """
          DELETE
          FROM favorite_authors
          WHERE marked_as_favorite_by_id = :id
          AND favorite_author_id = :authorId;
          """, nativeQuery = true)
    void removePersonFromPersonFavoritesAuthors(UUID id, UUID authorId);


    Boolean existsByIdAndFavoriteAuthorsId(UUID id, UUID favoriteAuthors_id);
}
