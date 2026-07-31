package com.bookhub.profileservice.adapters;

import com.bookhub.profileservice.config.properties.SecurityOriginProperties;
import com.bookhub.profileservice.enums.UserRole;
import com.bookhub.profileservice.exceptions.extensions.RemoteServiceException;
import com.bookhub.profileservice.ports.BookProvisioningPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class BookRestAdapter extends RestAdapter implements BookProvisioningPort {

    private final RestClient restClient;

    private final SecurityOriginProperties securityOriginProperties;
    @Value("${services.book-service.url}")
    private String baseUrl;

    @Override
    public void verifyBookAvailability(String userId, String bookId) {
        restClient.get()
                .uri("%s/api/v1/books/{uuid}/star".formatted(baseUrl), bookId)
                .header(GATEWAY_VERIFICATION_HEADER_NAME, securityOriginProperties.getGatewaySecret())
                .header(INTERNAL_VERIFICATION_HEADER_NAME, securityOriginProperties.getInternalSecret())
                .header(USER_ID_HEADER_NAME, userId)
                .header(USER_ROLE_HEADER_NAME, UserRole.READER.name())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new RemoteServiceException(new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8), response.getStatusCode());
                })
                .toBodilessEntity();
    }

    @Override
    public void addStar(UUID bookId) {

    }

    @Override
    public void removeStar(UUID bookId) {

    }
}
