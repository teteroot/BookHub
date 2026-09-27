package com.bookhub.profileservice;


import com.bookhub.profileservice.exceptions.extensions.RemoteServerErrorException;
import com.bookhub.profileservice.ports.BookProvisioningPort;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.wiremock.spring.EnableWireMock;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock
@TestPropertySource(properties = {
        "services.book-service.url=http://localhost:${wiremock.server.port}",
        "rest-client.timeout.connect-timeout=50ms",
        "rest-client.timeout.read-timeout=100ms"
})
public class BookRestAdapterIntegrationTests {

    @Autowired
    private BookProvisioningPort bookProvisioningPort;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @AfterEach
    void tearDown() {
        WireMock.reset();
        circuitBreakerRegistry.getAllCircuitBreakers()
                .forEach(CircuitBreaker::reset);
    }

    @Test
    void testRetryWhenExternalServiceReturnsError() {
        var personId = UUID.randomUUID();
        var bookId = UUID.randomUUID();
        stubFor(get("/api/v1/books/%s/availability".formatted(bookId))
                .willReturn(aResponse().withStatus(500)));
        assertThrows(RemoteServerErrorException.class, () -> bookProvisioningPort.verifyBookAvailability(personId.toString(),bookId.toString()));
        verify(3, getRequestedFor(urlEqualTo("/api/v1/books/%s/availability".formatted(bookId))));
    }

    @Test
    void testRetryWhenExternalServiceFault() {
        var personId = UUID.randomUUID();
        var bookId = UUID.randomUUID();
        stubFor(get("/api/v1/books/%s/availability".formatted(bookId))
                .willReturn(aResponse().withFixedDelay(10000)));
        assertThrows(RemoteServerErrorException.class, () -> bookProvisioningPort.verifyBookAvailability(personId.toString(),bookId.toString()));
        verify(3, getRequestedFor(urlEqualTo("/api/v1/books/%s/availability".formatted(bookId))));
    }

}
