package com.bookhub.profileservice.controllers;

import com.bookhub.profileservice.dtos.responses.PersonResponseDto;
import com.bookhub.profileservice.mappers.PersonMapper;
import com.bookhub.profileservice.security.GatewayUserDetails;
import com.bookhub.profileservice.services.FavoritesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<Void> addToFavoriteAuthors(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                               @PathVariable UUID uuid){
        favoritesService.addToFavoriteAuthors(userDetails.getUserId(),uuid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/authors/{uuid}")
    public ResponseEntity<Void> removeFromFavoriteAuthors(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                    @PathVariable UUID uuid){
        favoritesService.removeFromFavoriteAuthors(userDetails.getUserId(),uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/books")
    public ResponseEntity<List<UUID>> getMyFavoriteBooks(@AuthenticationPrincipal GatewayUserDetails userDetails){
        return ResponseEntity.ok(favoritesService.loadFavoriteBooks(userDetails.getUserId()));
    }

    @PostMapping("/books/{uuid}")
    public ResponseEntity<Void> addToFavoriteBooks(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                               @PathVariable UUID uuid){
        favoritesService.addBookToFavoriteBooks(userDetails.getUserId(),uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/books/{uuid}")
    public ResponseEntity<Void> removeFromFavoriteBooks(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                        @PathVariable UUID uuid){
        favoritesService.removeFromFavoriteBooks(userDetails.getUserId(),uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('INTERNAL')")
    @DeleteMapping("/books/{uuid}/references")
    public ResponseEntity<Void> removeFavoriteBookReferences(@PathVariable UUID uuid){
        favoritesService.removeFavoriteBookReferences(uuid);
        return ResponseEntity.noContent().build();
    }

}
