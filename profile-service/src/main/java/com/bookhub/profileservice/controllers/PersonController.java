package com.bookhub.profileservice.controllers;

import com.bookhub.profileservice.dtos.requests.PersonCreateRequestDto;
import com.bookhub.profileservice.dtos.requests.PersonUpdateRequestDto;
import com.bookhub.profileservice.dtos.responses.BiographyResponseDto;
import com.bookhub.profileservice.dtos.responses.PersonResponseDto;
import com.bookhub.profileservice.exceptions.extensions.IncorrectRequestDataException;
import com.bookhub.profileservice.mappers.PersonMapper;
import com.bookhub.profileservice.security.GatewayUserDetails;
import com.bookhub.profileservice.services.PersonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/persons")
public class PersonController {

    private final PersonMapper personMapper;
    private final PersonService personService;

    @PostMapping
    public ResponseEntity<Void> createPerson(@RequestBody @Valid PersonCreateRequestDto personCreateRequestDto,
                                             BindingResult result){
        if (result.hasErrors()) throw new IncorrectRequestDataException(result);
        var person = personMapper.toPerson(personCreateRequestDto);
        personService.createPerson(person);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<String> getMyUUID(@AuthenticationPrincipal GatewayUserDetails userDetails){
        return ResponseEntity.ok(userDetails.getUserId().toString());
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<PersonResponseDto>> getMyFavoritesAuthors(@AuthenticationPrincipal GatewayUserDetails userDetails){
        var dto = personService.loadFavorites(userDetails.getUserId())
                .stream().map(personMapper::toDto).toList();
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/favorites/{uuid}")
    public ResponseEntity<Void> addToFavorites(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                                         @PathVariable UUID uuid){
        personService.addToFavorites(userDetails.getUserId(),uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/favorites/{uuid}")
    public ResponseEntity<Void> removeFromFavorites(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                               @PathVariable UUID uuid){
        personService.removeFromFavorites(userDetails.getUserId(),uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<PagedModel<PersonResponseDto>> searchPerson(@RequestParam String query,
                                                                   @RequestParam Integer page){
        var dto = personService.searchPersons(query,page,10)
                .map(personMapper::toDto);
        return ResponseEntity.ok(new PagedModel<>(dto));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<PersonResponseDto> getPerson(@PathVariable UUID uuid){
        var dto = personMapper.toDto(personService.loadPersonByUUID(uuid));
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{uuid}/biography")
    public ResponseEntity<BiographyResponseDto> getPersonBiography(@PathVariable UUID uuid){
        return ResponseEntity.ok(personService.loadPersonBiographyByUUID(uuid));
    }

    @PatchMapping("/me")
    public ResponseEntity<Void> updatePerson(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                               @RequestBody @Valid PersonUpdateRequestDto personUpdateRequestDto,
                                               BindingResult result){
        if (result.hasErrors()) throw new IncorrectRequestDataException(result);
        var person = personMapper.toPerson(personUpdateRequestDto);
        personService.updatePerson(userDetails.getUserId(),person);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
