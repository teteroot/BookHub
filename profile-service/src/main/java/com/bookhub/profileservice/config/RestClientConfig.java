package com.bookhub.profileservice.config;

import com.bookhub.profileservice.config.properties.RestClientTimeoutProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Configuration
public class RestClientConfig {

    private final RestClientTimeoutProperties restClientTimeoutProperties;

    @Bean
    public RestClient restClient(){
        var simpleClient = new SimpleClientHttpRequestFactory();
        simpleClient.setConnectTimeout(restClientTimeoutProperties.getConnectTimeout());
        simpleClient.setReadTimeout(restClientTimeoutProperties.getReadTimeout());
        return RestClient.builder()
                .requestFactory(simpleClient)
                .build();

    }

}
