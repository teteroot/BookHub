package com.bookhub.bookservice.adapters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public abstract class RestAdapter {

    protected final String GATEWAY_VERIFICATION_HEADER_NAME = "X-Gateway-Secret";
    protected final String INTERNAL_VERIFICATION_HEADER_NAME = "X-Internal-Secret";
    protected final String USER_ID_HEADER_NAME = "X-User-Id";
    protected final String USER_ROLE_HEADER_NAME = "X-User-Role";

}