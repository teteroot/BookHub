package com.bookhub.apigateway.managers;

import com.bookhub.apigateway.properties.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;


@RequiredArgsConstructor
@Component
public class BucketManager {

    private final RateLimitProperties rateLimitProperties;

    //                              service/bucket
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket getBucket(String serviceId){
        if (!buckets.containsKey(serviceId)){
            var bucket = Bucket.builder()
                    .addLimit(
                            Bandwidth.builder()
                                    .capacity(rateLimitProperties.getTokenCapacity())
                                    .refillIntervally(rateLimitProperties.getTokenCapacity(), rateLimitProperties.getDuration())
                                    .build()
                    )
                    .build();
            buckets.put(serviceId, bucket);
        }
        return buckets.get(serviceId);
    }

}
