package com.bookhub.profileservice.repositories;

import com.bookhub.profileservice.dtos.responses.BiographyResponseDto;
import com.bookhub.profileservice.models.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Page<Person> findByFirstNameAndLastNameOrLastNameAndLastName(String firstName, String lastName, Pageable pageable);
    @Query("""
           SELECT p
           FROM Person p
           WHERE LOWER(p.firstName) LIKE LOWER(CONCAT(:name, "%"))
           OR LOWER(p.lastName) LIKE LOWER(CONCAT(:name, "%"))
           """)
    Page<Person> findByFirstNameOrLastName(String name,
                                           Pageable pageable);
}
