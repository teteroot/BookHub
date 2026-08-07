package com.bookhub.bookservice.adapters;

import com.bookhub.bookservice.config.properties.SecurityOriginProperties;
import com.bookhub.bookservice.enums.UserRole;
import com.bookhub.bookservice.exceptions.extensions.RemoteInternalServerErrorException;
import com.bookhub.bookservice.exceptions.extensions.RemoteServiceException;
import com.bookhub.bookservice.ports.ProfileProvisioningPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Component
public class ProfileRestAdapter extends RestAdapter implements ProfileProvisioningPort {

    private final RestClient restClient;

    private final SecurityOriginProperties securityOriginProperties;
    @Value("${services.profile-service.url}")
    private String baseUrl;

    @Override
    public void removeBookReferencesFromAllFavorites(String bookId, String authorId) {
        try {
            restClient.delete()
                    .uri("%s/api/v1/persons/favorites/books/{uuid}/references".formatted(baseUrl), bookId)
                    .header(GATEWAY_VERIFICATION_HEADER_NAME, securityOriginProperties.getGatewaySecret())
                    .header(INTERNAL_VERIFICATION_HEADER_NAME, securityOriginProperties.getInternalSecret())
                    .header(USER_ID_HEADER_NAME, authorId)
                    .header(USER_ROLE_HEADER_NAME, UserRole.READER.name())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new RemoteServiceException(new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8), response.getStatusCode());
                    })
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            throw new RemoteInternalServerErrorException();
        }
    }
}
