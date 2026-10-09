package com.bookhub.authservice.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Data
@ConfigurationProperties(prefix = "rest-client.timeout")
public class RestClientTimeoutProperties {
    private Duration connectTimeout;
    private Duration readTimeout;
}
