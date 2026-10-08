package com.bookhub.profileservice.config;

import com.bookhub.profileservice.config.properties.RestClientTimeoutProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@RequiredArgsConstructor
@Configuration
public class RestClientConfig {

    private final RestClientTimeoutProperties restClientTimeoutProperties;

    @Bean
    public RestClient restClient(){
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(restClientTimeoutProperties.getConnectTimeout())
                .build();
        var factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(restClientTimeoutProperties.getReadTimeout());
        return RestClient.builder()
                .requestFactory(factory)
                .build();

    }

}
