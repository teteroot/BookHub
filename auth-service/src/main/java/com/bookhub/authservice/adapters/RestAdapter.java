package com.bookhub.authservice.adapters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public abstract class RestAdapter {

    protected final String GATEWAY_VERIFICATION_HEADER_NAME = "X-Gateway-Secret";

    @Value("${security.origin.gateway.secret}")
    protected String GATEWAY_VERIFICATION_SECRET;

}
