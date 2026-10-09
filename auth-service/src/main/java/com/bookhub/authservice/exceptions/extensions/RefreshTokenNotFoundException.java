package com.bookhub.authservice.exceptions.extensions;

import com.bookhub.authservice.exceptions.NotFoundException;

public class RefreshTokenNotFoundException extends NotFoundException {
    public RefreshTokenNotFoundException() {
        super("Refresh token not found");
    }
}
