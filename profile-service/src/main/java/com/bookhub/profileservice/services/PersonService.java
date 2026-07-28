package com.bookhub.profileservice.services;

import com.bookhub.profileservice.dtos.requests.PersonUpdateRequestDto;
import com.bookhub.profileservice.dtos.responses.BiographyResponseDto;
import com.bookhub.profileservice.models.Person;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface PersonService {
    void createPerson(UUID uuid,Person person);
    Page<Person> searchPersons(String query, int page, int size);
    Person loadPersonByUUID(UUID uuid);
    BiographyResponseDto loadPersonBiographyByUUID(UUID uuid);
    void updatePerson(UUID id, PersonUpdateRequestDto person);
    Page<Person> loadAuthors(int page, int size);
}
