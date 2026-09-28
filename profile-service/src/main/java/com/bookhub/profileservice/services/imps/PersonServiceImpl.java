package com.bookhub.profileservice.services.imps;

import com.bookhub.profileservice.dtos.requests.PersonUpdateRequestDto;
import com.bookhub.profileservice.dtos.responses.BiographyResponseDto;
import com.bookhub.profileservice.enums.UserRole;
import com.bookhub.profileservice.exceptions.extensions.BiographyNotFoundException;
import com.bookhub.profileservice.exceptions.extensions.PersonAlreadyExistsException;
import com.bookhub.profileservice.exceptions.extensions.PersonNotFoundException;
import com.bookhub.profileservice.mappers.PersonMapper;
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
    private final PersonMapper personMapper;

    @Override
    @Transactional
    public void createPerson(UUID uuid,Person person) {
        if (personRepository.existsById(uuid)){
            throw new PersonAlreadyExistsException();
        }
        person.setId(uuid);
        person.setDateOfRegistration(Instant.now());
        personRepository.save(person);
    }

    @Override
    public Page<Person> searchPersons(String query, int page, int size) {
        String[] firstAndLastName = query.trim().split("\\s+",2);
        if (firstAndLastName.length == 2) {
            return personRepository.findByFirstNameAndLastNameOrLastNameAndFirstName(
                    firstAndLastName[0], firstAndLastName[1], PageRequest.of(page, size)
            );
        }
        return personRepository.findByFirstNameOrLastName(
                firstAndLastName[0],PageRequest.of(page, size)
        );

    }

    @Override
    @Transactional
    public void updatePerson(UUID id, PersonUpdateRequestDto updatedPerson) {
        var person = personRepository.findById(id)
                .orElseThrow(PersonNotFoundException::new);
        personMapper.updatePerson(person, updatedPerson);
        personRepository.save(person);
    }



    @Override
    @Transactional
    public Page<Person> loadAuthors(int page, int size) {
        return personRepository.findPersonByRoleGroupByMarkedAsFavoriteAuthorsIdSize(
                UserRole.AUTHOR,PageRequest.of(page,size)
        );
    }

    @Override
    public Person loadPersonByUUID(UUID uuid) {
        return personRepository.findById(uuid)
                .orElseThrow(PersonNotFoundException::new);
    }

    @Override
    public BiographyResponseDto loadPersonBiographyByUUID(UUID uuid) {
        return personRepository.findBiographyById(uuid)
                .orElseThrow(BiographyNotFoundException::new);

    }
}
