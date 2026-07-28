package com.bookhub.profileservice.services;

import com.bookhub.profileservice.models.Person;

import java.util.List;
import java.util.UUID;

public interface FavoritesService {

    List<Person> loadFavoriteAuthors(UUID userId);

    void addToFavoriteAuthors(UUID userId, UUID targetPersonId);

    void removeFromFavoriteAuthors(UUID userId, UUID targetPersonId);

}
