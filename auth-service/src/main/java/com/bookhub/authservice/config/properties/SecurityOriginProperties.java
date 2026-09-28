package com.bookhub.authservice.config.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("security.origin")
@Data
public class SecurityOriginProperties {
    private String gatewaySecret;
    private String internalSecret;
}
