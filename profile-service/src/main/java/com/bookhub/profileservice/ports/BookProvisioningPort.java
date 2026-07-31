package com.bookhub.profileservice.ports;

public interface BookProvisioningPort {

    void verifyBookAvailability(String personId, String bookId);

    void addStar(String personId, String bookId);

    void removeStar(String personId, String bookId);
}
