package com.bookhub.authservice.adapters;

import com.bookhub.authservice.dtos.requests.PersonDataRequestDto;
import com.bookhub.authservice.ports.ProfileProvisioningPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProfileRestAdapter implements ProfileProvisioningPort {
    @Override
    public void createPerson(PersonDataRequestDto personData) {
        //TODO REST request to profile-service
        log.debug("create request to profile-service");
    }
}
