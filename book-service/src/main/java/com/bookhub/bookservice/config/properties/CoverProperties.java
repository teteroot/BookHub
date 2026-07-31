package com.bookhub.bookservice.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@ConfigurationProperties(prefix = "spring.cloud.cover")
@Component
@Data
public class CoverProperties {

    private Set<MediaType> supportedTypes;
    private Double targetRatio;
    private Double ratioTolerance;
    private Integer dpi;
    private MediaType commonType;

    public void setCommonType(String commonType) {
        this.commonType = MediaType.parseMediaType(commonType);
    }

    public void setSupportedTypes(Set<String> supportedTypes) {
        this.supportedTypes = new HashSet<>();
        for (String type : supportedTypes) {
            this.supportedTypes.add(MediaType.parseMediaType(type));
        }
    }
}
