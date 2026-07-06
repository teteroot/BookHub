package com.bookhub.bookservice.config.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.cloud.storage")
@Component
@Data
public class StorageProperties {
    private String bucketName;
    private String contentType;
    private String destination;
    private String pageDestination;
    private String fileExtension;
}
