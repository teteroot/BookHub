package com.bookhub.profileservice.services;

import com.bookhub.profileservice.models.Person;

import java.util.List;
import java.util.UUID;

public interface FavoritesService {

    List<Person> loadFavoriteAuthors(UUID personId);

    void addToFavoriteAuthors(UUID personId, UUID targetPersonId);

    void removeFromFavoriteAuthors(UUID personId, UUID targetPersonId);

    void addBookToFavoriteBooks(UUID personId, UUID bookId);

    List<UUID> loadFavoriteBooks(UUID personId);

    void removeFromFavoriteBooks(UUID personId, UUID bookId);

    void removeFavoriteBookReferences(UUID bookId);
}
