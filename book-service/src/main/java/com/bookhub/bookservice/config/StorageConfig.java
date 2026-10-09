package com.bookhub.bookservice.config;

import com.bookhub.bookservice.config.properties.StorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RequiredArgsConstructor
@Configuration
public class StorageConfig {

    private final StorageProperties storageProperties;

    private final S3Client s3Client;

    @PostConstruct
    public void initBucket() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(storageProperties.getBucketName())
                    .build());
        } catch (NoSuchBucketException e) {
            try {
                s3Client.createBucket(CreateBucketRequest.builder()
                        .bucket(storageProperties.getBucketName())
                        .build());
            } catch (S3Exception s3Ex) {
                throw new IllegalStateException("Failed to create AWS S3 bucket: " + storageProperties.getBucketName(), s3Ex);
            }
        } catch (S3Exception e) {
            throw new IllegalStateException("Failed to verify AWS S3 bucket existence: " + storageProperties.getBucketName(), e);
        }
    }
}