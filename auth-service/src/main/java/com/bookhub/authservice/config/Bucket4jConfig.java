package com.bookhub.authservice.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "rate-limiting.bucket4j")
@Setter
public class Bucket4jConfig {

    private Integer tokenCapacity;
    private Duration duration;

    @PostConstruct
    void setUp(){
        log.info("Bucket4j configuration: capacity = {}, duration = {}", tokenCapacity, duration);
    }

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
