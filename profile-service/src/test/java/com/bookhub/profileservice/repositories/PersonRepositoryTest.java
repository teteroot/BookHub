package com.bookhub.profileservice.repositories;


import com.bookhub.profileservice.enums.UserRole;
import com.bookhub.profileservice.models.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@Transactional
public class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine3.22")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }


    Person setUpPerson(){
        var uuid = UUID.randomUUID();
        Person person = Person.builder()
                .id(uuid)
                .firstName("Test")
                .lastName("User")
                .biography("Biography")
                .build();
        personRepository.save(person);
        return person;
    }

    @Test
    void testFindBiographyById() {
        Person person = setUpPerson();
        assertEquals("Biography", (personRepository.findBiographyById(person.getId()).orElseThrow()).biography());
    }

    @Test
    void testFindByFirstNameAndLastNameOrLastNameAndLastName() {
        var person = setUpPerson();
        var firstPage = personRepository.findByFirstNameAndLastNameOrLastNameAndLastName(person.getFirstName(),person.getLastName(), PageRequest.of(0,1));
        assertEquals(1,firstPage.getTotalElements());
        assertEquals(person.getId(),firstPage.getContent().getFirst().getId());

        var secondPage = personRepository.findByFirstNameAndLastNameOrLastNameAndLastName(person.getLastName(),person.getFirstName(), PageRequest.of(0,1));
        assertEquals(1,secondPage.getTotalElements());
        assertEquals(person.getId(),secondPage.getContent().getFirst().getId());
    }

    @Test
    void testFindByFirstNameOrLastName() {
        var person = setUpPerson();
        var firstPage = personRepository.findByFirstNameOrLastName(person.getFirstName(), PageRequest.of(0,1));
        assertEquals(1,firstPage.getTotalElements());
        assertEquals(person.getId(),firstPage.getContent().getFirst().getId());

        var secondPage = personRepository.findByFirstNameOrLastName(person.getLastName(), PageRequest.of(0,1));
        assertEquals(1,secondPage.getTotalElements());
        assertEquals(person.getId(),secondPage.getContent().getFirst().getId());
    }

    @Test
    void testFindPersonByRoleGroupByMarkedAsFavoriteAuthorsIdSize() {
        var person1 = setUpPerson();
        person1.setRole(UserRole.AUTHOR);
        var person2 = setUpPerson();
        person2.setRole(UserRole.AUTHOR);
        var person3 = setUpPerson();
        person3.setRole(UserRole.AUTHOR);

        person2.setFavoriteAuthors(new HashSet<>(Set.of(person1,person3)));
        person1.setFavoriteAuthors(new HashSet<>(Set.of(person3)));
        person3.setFavoriteAuthors(new HashSet<>());
        personRepository.saveAll(List.of(person1,person2,person3));

        var page = personRepository.findPersonByRoleGroupByMarkedAsFavoriteAuthorsIdSize(UserRole.AUTHOR,PageRequest.of(0,3));
        assertEquals(3,page.getTotalElements());
        assertIterableEquals(List.of(person3,person1,person2),page.getContent());

        person2.setFavoriteAuthors(Collections.emptySet());
        person1.setFavoriteAuthors(Collections.emptySet());
        person3.setFavoriteAuthors(Collections.emptySet());
        personRepository.saveAll(List.of(person1,person2,person3));

    }


    @Test
    void existsByIdAndFavoriteAuthorsId() {
        var person1 = setUpPerson();
        person1.setRole(UserRole.AUTHOR);
        var person2 = setUpPerson();
        person2.setRole(UserRole.AUTHOR);
        personRepository.saveAll(List.of(person1,person2));
        assertFalse(personRepository.existsByIdAndFavoriteAuthorsId(person1.getId(),person2.getId()));
        personRepository.addPersonToFavoriteAuthors(person1.getId(),person2.getId());
        assertTrue(personRepository.existsByIdAndFavoriteAuthorsId(person1.getId(),person2.getId()));
    }
}
