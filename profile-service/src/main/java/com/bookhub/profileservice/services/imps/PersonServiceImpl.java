package com.bookhub.profileservice.services.imps;

import com.bookhub.profileservice.dtos.responses.BiographyResponseDto;
import com.bookhub.profileservice.enums.UserRole;
import com.bookhub.profileservice.exceptions.extensions.BiographyNotFoundException;
import com.bookhub.profileservice.exceptions.extensions.PersonAlreadyInFavoritesException;
import com.bookhub.profileservice.exceptions.extensions.PersonNotFoundException;
import com.bookhub.profileservice.exceptions.extensions.SelfRequestException;
import com.bookhub.profileservice.models.Person;
import com.bookhub.profileservice.repositories.PersonRepository;
import com.bookhub.profileservice.services.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
    @Transactional
    public List<Person> loadFavorites(UUID userId) {
        var person = personRepository.findById(userId)
                .orElseThrow(PersonNotFoundException::new);
        return new ArrayList<>(person.getFavoriteAuthors());
    }

    @Override
    @Transactional
    public void addToFavorites(UUID userId, UUID targetPersonId) {
        if (userId.equals(targetPersonId)){
            throw new SelfRequestException();
        }
        var person = personRepository.findById(userId)
                .orElseThrow(PersonNotFoundException::new);
        var targetPerson = personRepository.findById(targetPersonId)
                .orElseThrow(PersonNotFoundException::new);
        if (personRepository.existsByIdAndFavoriteAuthorsId(userId, targetPersonId)) {
            throw new PersonAlreadyInFavoritesException();
        }
        person.getFavoriteAuthors().add(targetPerson);
        personRepository.save(person);
    }

    @Override
    @Transactional
    public void removeFromFavorites(UUID userId, UUID targetPersonId) {
        if (userId.equals(targetPersonId)){
            throw new SelfRequestException();
        }
        var person = personRepository.findById(userId)
                .orElseThrow(PersonNotFoundException::new);
        var targetPerson = personRepository.findById(targetPersonId)
                .orElseThrow(PersonNotFoundException::new);
        if (!personRepository.existsByIdAndFavoriteAuthorsId(userId, targetPersonId)) {
            throw new PersonNotFoundException();
        }
        person.getFavoriteAuthors().remove(targetPerson);
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
