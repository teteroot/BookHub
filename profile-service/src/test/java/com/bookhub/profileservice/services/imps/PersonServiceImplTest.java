package com.bookhub.profileservice.services.imps;

import com.bookhub.profileservice.models.Person;
import com.bookhub.profileservice.repositories.PersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonServiceImpl personService;


    @Test
    void testSuccessfulCreatePerson() {
        assertDoesNotThrow(()-> personService.createPerson(UUID.randomUUID(),new Person()));
        verify(personRepository, times(1)).save(any(Person.class));
    }
}