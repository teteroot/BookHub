package com.bookhub.profileservice.services.imps;

import com.bookhub.profileservice.exceptions.extensions.PersonAlreadyInFavoritesException;
import com.bookhub.profileservice.exceptions.extensions.PersonNotFoundException;
import com.bookhub.profileservice.exceptions.extensions.SelfRequestException;
import com.bookhub.profileservice.models.Person;
import com.bookhub.profileservice.repositories.PersonRepository;
import com.bookhub.profileservice.services.FavoritesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FavoritesServiceImpl implements FavoritesService {

    private final PersonRepository personRepository;

    @Override
    @Transactional
    public List<Person> loadFavoriteAuthors(UUID userId) {
        var person = personRepository.findById(userId)
                .orElseThrow(PersonNotFoundException::new);
        return new ArrayList<>(person.getFavoriteAuthors());
    }

    @Override
    @Transactional
    public void addToFavoriteAuthors(UUID userId, UUID targetPersonId) {
        if (userId.equals(targetPersonId)){
            throw new SelfRequestException();
        }
        if (!personRepository.existsById(userId) || !personRepository.existsById(targetPersonId)){
            throw new PersonNotFoundException();
        }
        if (personRepository.existsByIdAndFavoriteAuthorsId(userId, targetPersonId)) {
            throw new PersonAlreadyInFavoritesException();
        }
        personRepository.addPersonToFavoriteAuthors(userId, targetPersonId);
    }

    @Override
    @Transactional
    public void removeFromFavoriteAuthors(UUID userId, UUID targetPersonId) {
        if (userId.equals(targetPersonId)){
            throw new SelfRequestException();
        }
        if (!personRepository.existsById(userId) || !personRepository.existsById(targetPersonId)){
            throw new PersonNotFoundException();
        }
        if (!personRepository.existsByIdAndFavoriteAuthorsId(userId, targetPersonId)) {
            throw new PersonNotFoundException();
        }
        personRepository.removePersonFromPersonFavoritesAuthors(userId, targetPersonId);
    }

}
