package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.services.BookStorageService;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BookStorageServiceImpl implements BookStorageService {

    private final S3Template s3Template;

    @Value("${spring.cloud.bucket-name}")
    private String bucketName;
    @Value("${spring.cloud.content-type}")
    private String contentType;

    private final static String DESTINATION = "/books/%s/content.pdf";


    @Override
    public String updateContent(UUID bookId, InputStream content, Long size) {
        var path = DESTINATION.formatted(bookId);
        s3Template.upload(bucketName,
                path,
                content,
                ObjectMetadata.builder()
                        .contentType(contentType)
                        .contentLength(size)
                        .build()
                );
        return path;
    }

}
