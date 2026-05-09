package com.bookhub.profileservice.controllers;

import com.bookhub.profileservice.dtos.requests.PersonCreateRequestDto;
import com.bookhub.profileservice.exceptions.extensions.IncorrectRequestDataException;
import com.bookhub.profileservice.mappers.PersonMapper;
import com.bookhub.profileservice.services.PersonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/persons")
public class PersonController {

    private final PersonMapper personMapper;
    private final PersonService personService;

    @PostMapping("/")
    public ResponseEntity<Void> createPerson(@RequestBody @Valid PersonCreateRequestDto personCreateRequestDto,
                                             BindingResult result){
        if (result.hasErrors()) throw new IncorrectRequestDataException(result);
        var person = personMapper.toPerson(personCreateRequestDto);
        personService.createPerson(person);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
