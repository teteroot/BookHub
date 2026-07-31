package com.bookhub.profileservice.services.imps;

import com.bookhub.profileservice.exceptions.extensions.*;
import com.bookhub.profileservice.models.FavoriteBook;
import com.bookhub.profileservice.models.Person;
import com.bookhub.profileservice.ports.BookProvisioningPort;
import com.bookhub.profileservice.repositories.FavoriteBookRepository;
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
    private final BookProvisioningPort bookProvisioningPort;
    private final FavoriteBookRepository favoriteBookRepository;

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

    @Override
    public List<UUID> loadFavoriteBooks(UUID personId) {
        return favoriteBookRepository.findAllByPersonIdOrderByCreatedAtDesc(personId)
                .stream().map(FavoriteBook::getBookId).toList();
    }

    @Override
    public void addBookToFavoriteBooks(UUID personId, UUID bookId) {
        if (favoriteBookRepository.existsByPersonIdAndBookId(personId,bookId)){
            throw new BookAlreadyInFavoritesException();
        }
        if (!personRepository.existsById(personId)){
            throw new PersonNotFoundException();
        }
        bookProvisioningPort.verifyBookAvailability(personId.toString(),bookId.toString());
        var favoriteBook = FavoriteBook.builder()
                .bookId(bookId)
                .personId(personId)
                .build();
        favoriteBookRepository.save(favoriteBook);
        bookProvisioningPort.addStar(personId.toString(),bookId.toString());
    }

    @Override
    public void removeFromFavoriteBooks(UUID personId, UUID bookId) {
        if (!personRepository.existsById(personId)){
            throw new PersonNotFoundException();
        }
        if (favoriteBookRepository.deleteByPersonIdAndBookId(personId, bookId) == 0){
            throw new BookNotInFavoritesException();
        }
        bookProvisioningPort.removeStar(personId.toString(),bookId.toString());
    }

}
