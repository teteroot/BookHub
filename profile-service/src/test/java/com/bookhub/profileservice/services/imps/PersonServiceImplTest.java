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
        when(personRepository.findByFirstNameAndLastNameOrLastNameAndFirstName("test","test", PageRequest.of(0,1)))
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