package com.bookhub.apigateway.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@ConfigurationProperties(prefix = "security")
public class SecurityOriginProperties {
    private String gatewaySecret;
    private String jwtKey;
    private List<String> securedHeaders;
}
