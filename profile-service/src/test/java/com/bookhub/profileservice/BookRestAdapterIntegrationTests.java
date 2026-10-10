package com.bookhub.profileservice;


import com.bookhub.profileservice.exceptions.extensions.RemoteServerErrorException;
import com.bookhub.profileservice.ports.BookProvisioningPort;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.wiremock.spring.EnableWireMock;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock
@TestPropertySource(properties = {
        "services.book-service.url=http://localhost:${wiremock.server.port}",
        "services.auth-service.url=http://localhost:${wiremock.server.port}",
        "rest-client.timeout.connect-timeout=50ms",
        "rest-client.timeout.read-timeout=100ms"
})
@Import(TestcontainersConfiguration.class)
public class BookRestAdapterIntegrationTests {

    @Autowired
    private BookProvisioningPort bookProvisioningPort;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Value("${resilience4j.circuitbreaker.instances.bookService.minimumNumberOfCalls}")
    private Integer cbNumberOfCalls;

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

    @Test
    void testRetryWhenExternalServiceResume() {
        var personId = UUID.randomUUID();
        var bookId = UUID.randomUUID();
        String secondAttemptName = "Second attempt";
        String thirdAttemptName = "Third attempt";
        stubFor(get("/api/v1/books/%s/availability".formatted(bookId))
                .inScenario("resume")
                .whenScenarioStateIs(Scenario.STARTED)
                .willSetStateTo(secondAttemptName)
                .willReturn(aResponse().withFixedDelay(10000)));
        stubFor(get("/api/v1/books/%s/availability".formatted(bookId))
                .inScenario("resume")
                .whenScenarioStateIs(secondAttemptName)
                .willSetStateTo(thirdAttemptName)
                .willReturn(aResponse().withFixedDelay(10000)));
        stubFor(get("/api/v1/books/%s/availability".formatted(bookId))
                .inScenario("resume")
                .whenScenarioStateIs(thirdAttemptName)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("true"))
        );

        assertDoesNotThrow(() -> bookProvisioningPort.verifyBookAvailability(personId.toString(),bookId.toString()));
        verify(3, getRequestedFor(urlEqualTo("/api/v1/books/%s/availability".formatted(bookId))));
    }

    @Test
    void testCancelRequestsWhenExternalServiceClose() {
        var personId = UUID.randomUUID();
        var bookId = UUID.randomUUID();
        stubFor(get("/api/v1/books/%s/availability".formatted(bookId))
                .willReturn(aResponse().withStatus(503)));
        assertThrows(RemoteServerErrorException.class,() -> bookProvisioningPort.verifyBookAvailability(personId.toString(),bookId.toString()));
        assertThrows(CallNotPermittedException.class,() -> bookProvisioningPort.verifyBookAvailability(personId.toString(),bookId.toString()));

        verify(cbNumberOfCalls, getRequestedFor(urlEqualTo("/api/v1/books/%s/availability".formatted(bookId))));
    }

}
