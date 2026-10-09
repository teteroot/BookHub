package com.bookhub.apigateway.properties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "rate-limiting.bucket4j")
@Data
public class RateLimitProperties {
    private Integer tokenCapacity;
    private Duration duration;
}
