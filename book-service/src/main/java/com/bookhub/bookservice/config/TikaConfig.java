package com.bookhub.bookservice.config;

import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TikaConfig {

    @Bean
    public Detector detector() {
        return new DefaultDetector();
    }
}
