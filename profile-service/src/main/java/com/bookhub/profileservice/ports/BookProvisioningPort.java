package com.bookhub.profileservice.ports;

import java.util.UUID;

public interface BookProvisioningPort {

    void verifyBookAvailability(String userId, String bookId);

    void addStar(UUID bookId);

    void removeStar(UUID bookId);
}
