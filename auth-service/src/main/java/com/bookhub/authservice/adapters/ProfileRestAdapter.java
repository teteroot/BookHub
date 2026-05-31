package com.bookhub.authservice.adapters;

import com.bookhub.authservice.dtos.requests.PersonDataRequestDto;
import com.bookhub.authservice.ports.ProfileProvisioningPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Slf4j
@Component
public class ProfileRestAdapter extends RestAdapter implements ProfileProvisioningPort {

    private final RestClient restClient;
    @Value("${services.profile-service.url}")
    private String baseUrl;
    @Override
    public void createPerson(String id, PersonDataRequestDto personData) {
        restClient.post()
                .uri("%s/api/v1/persons".formatted(baseUrl))
                .header(GATEWAY_VERIFICATION_HEADER_NAME,GATEWAY_VERIFICATION_SECRET)
                .header(USER_ID_HEADER_NAME, id)
                .header(USER_ROLE_HEADER_NAME, personData.getRole().name())
                .contentType(MediaType.APPLICATION_JSON)
                .body(personData)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,(request, response) -> {
                    throw new ResponseStatusException(response.getStatusCode(),new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8));
                })
                .toBodilessEntity();
    }
}
