package com.bookhub.apigateway.managers;

import com.bookhub.apigateway.properties.RateLimitProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;


@RequiredArgsConstructor
@Component
public class BucketManager {

    private final RateLimitProperties rateLimitProperties;

    //                  service/bucket
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(100_000)
            .build();

    public Bucket getBucket(String serviceId, String host){
        String bucketName = "%s:%s".formatted(serviceId, host);
        return buckets.get(bucketName, bkName -> Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(rateLimitProperties.getTokenCapacity())
                                .refillIntervally(rateLimitProperties.getTokenCapacity(), rateLimitProperties.getDuration())
                                .build()
                ).build());
    }

}
