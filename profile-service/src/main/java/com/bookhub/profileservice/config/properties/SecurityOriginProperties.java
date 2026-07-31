package com.bookhub.profileservice.config.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "security.origin")
public class SecurityOriginProperties {

    private String gatewaySecret;

    private String internalSecret;
}
