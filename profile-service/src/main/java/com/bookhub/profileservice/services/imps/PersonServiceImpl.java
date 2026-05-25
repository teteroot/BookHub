package com.bookhub.profileservice.services.imps;

import com.bookhub.profileservice.dtos.responses.BiographyResponseDto;
import com.bookhub.profileservice.exceptions.extensions.BiographyNotFoundException;
import com.bookhub.profileservice.exceptions.extensions.PersonNotFoundException;
import com.bookhub.profileservice.models.Person;
import com.bookhub.profileservice.repositories.PersonRepository;
import com.bookhub.profileservice.services.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    @Override
    @Transactional
    public void createPerson(Person person) {
        person.setDateOfRegistration(Instant.now());
        personRepository.save(person);
    }

    @Override
    public Page<Person> searchPersons(String query, int page, int size) {
        String[] firstAndLastName = query.split(" ", 2);
        if (firstAndLastName.length == 2) {
            return personRepository.findByFirstNameAndLastNameOrLastNameAndLastName(
                    firstAndLastName[0], firstAndLastName[1], PageRequest.of(page, size)
            );
        }
        return personRepository.findByFirstNameOrLastName(
                firstAndLastName[0],PageRequest.of(page, size)
        );

    }

    @Override
    @Transactional
    public void updatePerson(UUID id, Person updatedPerson) {
        var person = personRepository.findById(id)
                .orElseThrow(BiographyNotFoundException::new);
        updatedPerson.setId(id);
        updatedPerson.setDateOfRegistration(person.getDateOfRegistration());
        personRepository.save(updatedPerson);
    }

    @Override
    public Person loadPersonByUUID(String uuid) {
        try {
            var id = UUID.fromString(uuid);
            return personRepository.findById(id)
                    .orElseThrow(PersonNotFoundException::new);
        } catch (IllegalArgumentException e) {
            throw new PersonNotFoundException();
        }
    }

    @Override
    public BiographyResponseDto loadPersonBiographyByUUID(String uuid) {
        try {
            var id = UUID.fromString(uuid);
            return personRepository.findBiographyById(id)
                    .orElseThrow(BiographyNotFoundException::new);
        } catch (IllegalArgumentException e) {
            throw new PersonNotFoundException();
        }
    }
}
