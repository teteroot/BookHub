package com.bookhub.profileservice.services.imps;

import com.bookhub.profileservice.dtos.requests.PersonUpdateRequestDto;
import com.bookhub.profileservice.dtos.responses.BiographyResponseDto;
import com.bookhub.profileservice.exceptions.extensions.*;
import com.bookhub.profileservice.mappers.PersonMapper;
import com.bookhub.profileservice.models.Person;
import com.bookhub.profileservice.repositories.PersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PersonMapper personMapper;

    @InjectMocks
    private PersonServiceImpl personService;


    @Test
    void testSuccessfulCreatePerson() {
        when(personRepository.existsById(any(UUID.class))).thenReturn(false);
        assertDoesNotThrow(()-> personService.createPerson(UUID.randomUUID(),new Person()));
        verify(personRepository, times(1)).save(any(Person.class));
    }

    @Test
    void testCreateAlreadyExistPerson() {
        when(personRepository.existsById(any(UUID.class))).thenReturn(true);
        assertThrows(PersonAlreadyExistsException.class,()-> personService.createPerson(UUID.randomUUID(),new Person()));
        verify(personRepository, times(0)).save(any(Person.class));
    }

    @Test
    void testSearchPersonsByName() {
        when(personRepository.findByFirstNameOrLastName("test", PageRequest.of(0,1)))
                .thenReturn(Page.empty());
        assertEquals(Page.empty(),personService.searchPersons("test",0,1));
    }


    @Test
    void testSearchPersonsByNameAndLastname() {
        when(personRepository.findByFirstNameAndLastNameOrLastNameAndLastName("test","test", PageRequest.of(0,1)))
                .thenReturn(Page.empty());
        assertEquals(Page.empty(),personService.searchPersons("test    test",0,1));
    }

    @Test
    void testUpdateNonExistPerson() {
        assertThrows(PersonNotFoundException.class,() -> personService.updatePerson(UUID.randomUUID(), new PersonUpdateRequestDto()));
    }

    @Test
    void testSuccessfulUpdatePerson() {
        var uuid = UUID.randomUUID();
        var dto = new PersonUpdateRequestDto();
        var person = new Person();
        when(personRepository.findById(uuid)).thenReturn(Optional.of(person));
        assertDoesNotThrow(() -> personService.updatePerson(uuid,dto));
        verify(personMapper, times(1)).updatePerson(person,dto);
        verify(personRepository, times(1)).save(person);
    }

    @Test
    void testSuccessfulLoadFavorites() {
        var uuid = UUID.randomUUID();
        var person1 = new Person();
        var person2 = new Person();
        person1.setFavoriteAuthors(new HashSet<>(Set.of(person2)));
        when(personRepository.findById(uuid)).thenReturn(Optional.of(person1));
        assertIterableEquals(List.of(person2),personService.loadFavorites(uuid));
    }

    @Test
    void testLoadEmptyFavorites() {
        var uuid = UUID.randomUUID();
        var person = new Person();
        person.setFavoriteAuthors(Collections.emptySet());
        when(personRepository.findById(uuid)).thenReturn(Optional.of(person));
        assertIterableEquals(Collections.emptyList(),personService.loadFavorites(uuid));
    }


    @Test
    void testLoadFavoritesForNonExistPerson() {
        assertThrows(PersonNotFoundException.class,() -> personService.loadFavorites(UUID.randomUUID()));
    }

    @Test
    void testSuccessfulAddToFavorites() {
        var uuid1 = UUID.randomUUID();
        var uuid2 = UUID.randomUUID();
        when(personRepository.existsById(uuid1)).thenReturn(true);
        when(personRepository.existsById(uuid2)).thenReturn(true);
        when(personRepository.existsByIdAndFavoriteAuthorsId(uuid1,uuid2))
                .thenReturn(false);
        assertDoesNotThrow(() -> personService.addToFavorites(uuid1,uuid2));
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
        assertThrows(PersonAlreadyInFavoritesException.class, () -> personService.addToFavorites(uuid1,uuid2));
    }

    @Test
    void testAddToFavoritesYourself() {
        var uuid = UUID.randomUUID();
        assertThrows(SelfRequestException.class,() -> personService.addToFavorites(uuid,uuid));
    }

    @Test
    void testAddToFavoritesNonExistPerson() {
        assertThrows(PersonNotFoundException.class,() -> personService.addToFavorites(UUID.randomUUID(),UUID.randomUUID()));
    }


    @Test
    void testSuccessfulRemoveFromFavorites() {
        var uuid1 = UUID.randomUUID();
        var uuid2 = UUID.randomUUID();
        when(personRepository.existsById(uuid1)).thenReturn(true);
        when(personRepository.existsById(uuid2)).thenReturn(true);
        when(personRepository.existsByIdAndFavoriteAuthorsId(uuid1,uuid2))
                .thenReturn(true);
        assertDoesNotThrow(() -> personService.removeFromFavorites(uuid1,uuid2));
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
        assertThrows(PersonNotFoundException.class, () -> personService.removeFromFavorites(uuid1,uuid2));
    }

    @Test
    void testRemoveFromFavoritesYourself() {
        var uuid = UUID.randomUUID();
        assertThrows(SelfRequestException.class,() -> personService.removeFromFavorites(uuid,uuid));
    }

    @Test
    void testRemoveFromFavoritesNonExistPerson() {
        assertThrows(PersonNotFoundException.class,() -> personService.removeFromFavorites(UUID.randomUUID(),UUID.randomUUID()));
    }


    @Test
    void testSuccessfulLoadAuthors() {
        when(personRepository.findPersonByRoleGroupByMarkedAsFavoriteAuthorsIdSize(
                any(),any()
        )).thenReturn(Page.empty());
        assertEquals(Page.empty(),personService.loadAuthors(0,1));
    }

    @Test
    void testSuccessfulLoadPersonByUUID() {
        var uuid = UUID.randomUUID();
        var person = new Person();
        when(personRepository.findById(uuid)).thenReturn(Optional.of(person));
        assertEquals(person,personService.loadPersonByUUID(uuid));
    }


    @Test
    void testLoadNonExistPersonByUUID() {
        assertThrows(PersonNotFoundException.class,() -> personService.loadPersonByUUID(UUID.randomUUID()));
    }

    @Test
    void testSuccessfulLoadPersonBiographyByUUID() {
        var uuid = UUID.randomUUID();
        when(personRepository.findBiographyById(uuid)).thenReturn(Optional.of(new BiographyResponseDto("test")));
        assertEquals(new BiographyResponseDto("test"),personService.loadPersonBiographyByUUID(uuid));
    }

    @Test
    void testLoadNonExistPersonBiographyByUUID() {
        var uuid = UUID.randomUUID();
        assertThrows(BiographyNotFoundException.class, () -> personService.loadPersonBiographyByUUID(uuid));
    }
}