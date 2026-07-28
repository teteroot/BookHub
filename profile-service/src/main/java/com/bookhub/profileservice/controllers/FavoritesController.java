package com.bookhub.profileservice.controllers;

import com.bookhub.profileservice.dtos.responses.PersonResponseDto;
import com.bookhub.profileservice.mappers.PersonMapper;
import com.bookhub.profileservice.security.GatewayUserDetails;
import com.bookhub.profileservice.services.FavoritesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/v1/persons/favorites")
@RestController
public class FavoritesController {

    private final FavoritesService favoritesService;
    private final PersonMapper personMapper;

    @GetMapping("/authors")
    public ResponseEntity<List<PersonResponseDto>> getMyFavoritesAuthors(@AuthenticationPrincipal GatewayUserDetails userDetails){
        var dto = favoritesService.loadFavoriteAuthors(userDetails.getUserId())
                .stream().map(personMapper::toDto).toList();
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/authors/{uuid}")
    public ResponseEntity<Void> addToFavorites(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                               @PathVariable UUID uuid){
        favoritesService.addToFavoriteAuthors(userDetails.getUserId(),uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/authors/{uuid}")
    public ResponseEntity<Void> removeFromFavorites(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                    @PathVariable UUID uuid){
        favoritesService.removeFromFavoriteAuthors(userDetails.getUserId(),uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
