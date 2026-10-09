package com.bookhub.bookservice.ports;

public interface ProfileProvisioningPort {

    void removeBookReferencesFromAllFavorites(String bookId, String authorId);
}
