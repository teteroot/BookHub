package com.bookhub.profileservice.services.imps;

import com.bookhub.profileservice.exceptions.extensions.PersonAlreadyInFavoritesException;
import com.bookhub.profileservice.exceptions.extensions.PersonNotFoundException;
import com.bookhub.profileservice.exceptions.extensions.SelfRequestException;
import com.bookhub.profileservice.models.Person;
import com.bookhub.profileservice.repositories.PersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoritesServiceImplTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private FavoritesServiceImpl favoritesService;

    @Test
    void testSuccessfulLoadFavorites() {
        var uuid = UUID.randomUUID();
        var person1 = new Person();
        var person2 = new Person();
        person1.setFavoriteAuthors(new HashSet<>(Set.of(person2)));
        when(personRepository.findById(uuid)).thenReturn(Optional.of(person1));
        assertIterableEquals(List.of(person2),favoritesService.loadFavoriteAuthors(uuid));
    }

    @Test
    void testLoadEmptyFavorites() {
        var uuid = UUID.randomUUID();
        var person = new Person();
        person.setFavoriteAuthors(Collections.emptySet());
        when(personRepository.findById(uuid)).thenReturn(Optional.of(person));
        assertIterableEquals(Collections.emptyList(),favoritesService.loadFavoriteAuthors(uuid));
    }


    @Test
    void testLoadFavoritesForNonExistPerson() {
        assertThrows(PersonNotFoundException.class,() -> favoritesService.loadFavoriteAuthors(UUID.randomUUID()));
    }

    @Test
    void testSuccessfulAddToFavorites() {
        var uuid1 = UUID.randomUUID();
        var uuid2 = UUID.randomUUID();
        when(personRepository.existsById(uuid1)).thenReturn(true);
        when(personRepository.existsById(uuid2)).thenReturn(true);
        when(personRepository.existsByIdAndFavoriteAuthorsId(uuid1,uuid2))
                .thenReturn(false);
        assertDoesNotThrow(() -> favoritesService.addToFavoriteAuthors(uuid1,uuid2));
        verify(personRepository, times(1)).addPersonToFavoriteAuthors(uuid1,uuid2);
    }

    @Test
    void testAddToFavoritesAlreadyFavoritePerson() {
        var uuid1 = UUID.randomUUID();
        var uuid2 = UUID.randomUUID();
        when(personRepository.existsById(uuid1)).thenReturn(true);
        when(personRepository.existsById(uuid2)).thenReturn(true);
        when(personRepository.existsByIdAndFavoriteAuthorsId(uuid1,uuid2))
                .thenReturn(true);
        assertThrows(PersonAlreadyInFavoritesException.class, () -> favoritesService.addToFavoriteAuthors(uuid1,uuid2));
    }

    @Test
    void testAddToFavoritesYourself() {
        var uuid = UUID.randomUUID();
        assertThrows(SelfRequestException.class,() -> favoritesService.addToFavoriteAuthors(uuid,uuid));
    }

    @Test
    void testAddToFavoritesNonExistPerson() {
        assertThrows(PersonNotFoundException.class,() -> favoritesService.addToFavoriteAuthors(UUID.randomUUID(),UUID.randomUUID()));
    }


    @Test
    void testSuccessfulRemoveFromFavorites() {
        var uuid1 = UUID.randomUUID();
        var uuid2 = UUID.randomUUID();
        when(personRepository.existsById(uuid1)).thenReturn(true);
        when(personRepository.existsById(uuid2)).thenReturn(true);
        when(personRepository.existsByIdAndFavoriteAuthorsId(uuid1,uuid2))
                .thenReturn(true);
        assertDoesNotThrow(() -> favoritesService.removeFromFavoriteAuthors(uuid1,uuid2));
        verify(personRepository, times(1)).removePersonFromPersonFavoritesAuthors(uuid1,uuid2);
    }

    @Test
    void testRemoveFromFavoritesNotFavoritePerson() {
        var uuid1 = UUID.randomUUID();
        var uuid2 = UUID.randomUUID();
        when(personRepository.existsById(uuid1)).thenReturn(true);
        when(personRepository.existsById(uuid2)).thenReturn(true);
        when(personRepository.existsByIdAndFavoriteAuthorsId(uuid1,uuid2))
                .thenReturn(false);
        assertThrows(PersonNotFoundException.class, () -> favoritesService.removeFromFavoriteAuthors(uuid1,uuid2));
    }

    @Test
    void testRemoveFromFavoritesYourself() {
        var uuid = UUID.randomUUID();
        assertThrows(SelfRequestException.class,() -> favoritesService.removeFromFavoriteAuthors(uuid,uuid));
    }

    @Test
    void testRemoveFromFavoritesNonExistPerson() {
        assertThrows(PersonNotFoundException.class,() -> favoritesService.removeFromFavoriteAuthors(UUID.randomUUID(),UUID.randomUUID()));
    }
}