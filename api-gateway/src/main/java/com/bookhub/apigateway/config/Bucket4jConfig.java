package com.bookhub.apigateway.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "rate-limiting.bucket4j")
@Setter
@Component
public class Bucket4jConfig {

    private Integer tokenCapacity;
    private Duration duration;


    @Bean
    public Bucket bucket() {
        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(tokenCapacity)
                                .refillIntervally(tokenCapacity, duration)
                                .build()
                )
                .build();
    }
}
