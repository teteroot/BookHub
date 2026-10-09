package com.bookhub.authservice.exceptions.extensions;

import com.bookhub.authservice.exceptions.BadRequestException;

public class RefreshTokenExpireException extends BadRequestException {
    public RefreshTokenExpireException() {
        super("Refresh token is expire. Please re-login.");
    }
}
