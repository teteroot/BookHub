package com.bookhub.profileservice.services;

import com.bookhub.profileservice.dtos.responses.BiographyResponseDto;
import com.bookhub.profileservice.models.Person;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface PersonService {
    void createPerson(Person person);
    Page<Person> searchPersons(String query, int page, int size);
    Person loadPersonByUUID(UUID uuid);
    BiographyResponseDto loadPersonBiographyByUUID(UUID uuid);
    void updatePerson(UUID id, Person person);
    List<Person> loadFavorites(UUID userId);
    void addToFavorite(UUID userId, UUID targetPersonId);
}
