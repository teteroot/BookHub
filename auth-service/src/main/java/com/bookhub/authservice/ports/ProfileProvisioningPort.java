package com.bookhub.authservice.ports;

import com.bookhub.authservice.dtos.requests.PersonDataRequestDto;

public interface ProfileProvisioningPort {
    void createPerson(PersonDataRequestDto personData);
}
