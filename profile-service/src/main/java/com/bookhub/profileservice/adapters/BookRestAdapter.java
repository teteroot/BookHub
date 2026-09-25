package com.bookhub.profileservice.adapters;

import com.bookhub.profileservice.config.properties.SecurityOriginProperties;
import com.bookhub.profileservice.enums.UserRole;
import com.bookhub.profileservice.exceptions.extensions.RemoteInternalServerErrorException;
import com.bookhub.profileservice.exceptions.extensions.RemoteServiceException;
import com.bookhub.profileservice.ports.BookProvisioningPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Component
public class BookRestAdapter extends RestAdapter implements BookProvisioningPort {

    private final RestClient restClient;

    private final SecurityOriginProperties securityOriginProperties;
    @Value("${services.book-service.url}")
    private String baseUrl;

    @Override
    @Retry(name = "bookService")
    @CircuitBreaker(name = "bookService")
    public void verifyBookAvailability(String personId, String bookId) {
        try {
            restClient.get()
                    .uri("%s/api/v1/books/{bookId}/availability".formatted(baseUrl), bookId)
                    .header(GATEWAY_VERIFICATION_HEADER_NAME, securityOriginProperties.getGatewaySecret())
                    .header(INTERNAL_VERIFICATION_HEADER_NAME, securityOriginProperties.getInternalSecret())
                    .header(USER_ID_HEADER_NAME, personId)
                    .header(USER_ROLE_HEADER_NAME, UserRole.READER.name())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new RemoteServiceException(new String(response.getBody().readNBytes(2048), StandardCharsets.UTF_8), response.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("Internal Server Error while book availability check: {}", new String (response.getBody().readNBytes(2048), StandardCharsets.UTF_8));
                        throw new RemoteInternalServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
                    })
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            throw new RemoteInternalServerErrorException(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    @CircuitBreaker(name = "bookService")
    public void addStar(String personId, String bookId) {
        try {
            restClient.post()
                    .uri("%s/api/v1/books/{bookId}/star".formatted(baseUrl), bookId)
                    .header(GATEWAY_VERIFICATION_HEADER_NAME, securityOriginProperties.getGatewaySecret())
                    .header(INTERNAL_VERIFICATION_HEADER_NAME, securityOriginProperties.getInternalSecret())
                    .header(USER_ID_HEADER_NAME, personId)
                    .header(USER_ROLE_HEADER_NAME, UserRole.READER.name())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new RemoteServiceException(new String(response.getBody().readNBytes(2048), StandardCharsets.UTF_8), response.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("Internal Server Error while add star: {}", new String (response.getBody().readNBytes(2048), StandardCharsets.UTF_8));
                        throw new RemoteInternalServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
                    })
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            throw new RemoteInternalServerErrorException(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    @Retry(name = "bookService")
    @CircuitBreaker(name = "bookService")
    public void removeStar(String personId, String bookId) {
        try {
            restClient.delete()
                    .uri("%s/api/v1/books/{bookId}/star".formatted(baseUrl), bookId)
                    .header(GATEWAY_VERIFICATION_HEADER_NAME, securityOriginProperties.getGatewaySecret())
                    .header(INTERNAL_VERIFICATION_HEADER_NAME, securityOriginProperties.getInternalSecret())
                    .header(USER_ID_HEADER_NAME, personId)
                    .header(USER_ROLE_HEADER_NAME, UserRole.READER.name())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new RemoteServiceException(new String(response.getBody().readNBytes(2048), StandardCharsets.UTF_8), response.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("Internal Server Error while remove star: {}", new String (response.getBody().readNBytes(2048), StandardCharsets.UTF_8));
                        throw new RemoteInternalServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
                    })
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            throw new RemoteInternalServerErrorException(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
