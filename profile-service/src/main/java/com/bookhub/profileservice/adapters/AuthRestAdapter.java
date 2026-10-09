package com.bookhub.profileservice.adapters;

import com.bookhub.profileservice.config.properties.SecurityOriginProperties;
import com.bookhub.profileservice.dtos.requests.UserRolePatchRequestDto;
import com.bookhub.profileservice.enums.UserRole;
import com.bookhub.profileservice.exceptions.extensions.RemoteServerErrorException;
import com.bookhub.profileservice.exceptions.extensions.RemoteServiceException;
import com.bookhub.profileservice.ports.AuthProvisioningPort;
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
public class AuthRestAdapter extends RestAdapter implements AuthProvisioningPort {

    private final RestClient restClient;

    private final SecurityOriginProperties securityOriginProperties;
    @Value("${services.auth-service.url}")
    private String baseUrl;

    @Override
    public void updateUserRole(String userId, String currentRole, UserRole newRole) {
        try {
            restClient.patch()
                    .uri("%s/api/v1/auth/users".formatted(baseUrl))
                    .header(GATEWAY_VERIFICATION_HEADER_NAME, securityOriginProperties.getGatewaySecret())
                    .header(INTERNAL_VERIFICATION_HEADER_NAME, securityOriginProperties.getInternalSecret())
                    .header(USER_ID_HEADER_NAME, userId)
                    .header(USER_ROLE_HEADER_NAME, currentRole)
                    .body(new UserRolePatchRequestDto(newRole))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new RemoteServiceException(new String(response.getBody().readNBytes(2048), StandardCharsets.UTF_8), response.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("Internal Server Error while updating user data: {}", new String (response.getBody().readNBytes(2048), StandardCharsets.UTF_8));
                        throw new RemoteServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
                    })
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            log.error("Server Access Error while updating user data: {}", e.getMessage());
            throw new RemoteServerErrorException(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
