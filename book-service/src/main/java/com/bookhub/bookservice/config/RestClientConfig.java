package com.bookhub.bookservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(){
        var simpleClient = new SimpleClientHttpRequestFactory();
        simpleClient.setConnectTimeout(Duration.ofSeconds(3));
        simpleClient.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder()
                .requestFactory(simpleClient)
                .build();

    }

}