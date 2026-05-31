package com.bookhub.authservice.adapters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public abstract class RestAdapter {

    protected final String GATEWAY_VERIFICATION_HEADER_NAME = "X-Gateway-Secret";
    protected final String USER_ID_HEADER_NAME = "X-User-Id";
    protected final String USER_ROLE_HEADER_NAME = "X-User-Role";

    @Value("${security.origin.gateway.secret}")
    protected String GATEWAY_VERIFICATION_SECRET;

}
