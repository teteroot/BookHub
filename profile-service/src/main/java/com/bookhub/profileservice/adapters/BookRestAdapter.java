package com.bookhub.profileservice.adapters;

import com.bookhub.profileservice.config.properties.SecurityOriginProperties;
import com.bookhub.profileservice.enums.UserRole;
import com.bookhub.profileservice.exceptions.extensions.RemoteInternalServerErrorException;
import com.bookhub.profileservice.exceptions.extensions.RemoteServiceException;
import com.bookhub.profileservice.ports.BookProvisioningPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Component
public class BookRestAdapter extends RestAdapter implements BookProvisioningPort {

    private final RestClient restClient;

    private final SecurityOriginProperties securityOriginProperties;
    @Value("${services.book-service.url}")
    private String baseUrl;

    @Override
    public void verifyBookAvailability(String personId, String bookId) {
        try {
            restClient.get()
                    .uri("%s/api/v1/books/{bookId}/availability".formatted(baseUrl), bookId)
                    .header(GATEWAY_VERIFICATION_HEADER_NAME, securityOriginProperties.getGatewaySecret())
                    .header(INTERNAL_VERIFICATION_HEADER_NAME, securityOriginProperties.getInternalSecret())
                    .header(USER_ID_HEADER_NAME, personId)
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

    @Override
    public void addStar(String personId, String bookId) {
        try {
            restClient.post()
                    .uri("%s/api/v1/books/{bookId}/star".formatted(baseUrl), bookId)
                    .header(GATEWAY_VERIFICATION_HEADER_NAME, securityOriginProperties.getGatewaySecret())
                    .header(INTERNAL_VERIFICATION_HEADER_NAME, securityOriginProperties.getInternalSecret())
                    .header(USER_ID_HEADER_NAME, personId)
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

    @Override
    public void removeStar(String personId, String bookId) {
        try {
            restClient.delete()
                    .uri("%s/api/v1/books/{bookId}/star".formatted(baseUrl), bookId)
                    .header(GATEWAY_VERIFICATION_HEADER_NAME, securityOriginProperties.getGatewaySecret())
                    .header(INTERNAL_VERIFICATION_HEADER_NAME, securityOriginProperties.getInternalSecret())
                    .header(USER_ID_HEADER_NAME, personId)
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
