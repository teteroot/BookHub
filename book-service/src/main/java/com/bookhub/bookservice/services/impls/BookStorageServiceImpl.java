package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.exceptions.extensions.ContentSaveException;
import com.bookhub.bookservice.config.properties.StorageProperties;
import com.bookhub.bookservice.services.BookStorageService;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BookStorageServiceImpl implements BookStorageService {

    private final S3Template s3Template;
    private final StorageProperties storageProperties;


    @Override
    public String createContent(UUID bookId, InputStream content, Long size) {
        var path = storageProperties.getDestination().formatted(bookId);
        try {
            s3Template.upload(storageProperties.getBucketName(),
                    path,
                    content,
                    ObjectMetadata.builder()
                            .contentType(storageProperties.getContentType())
                            .contentLength(size)
                            .build()
            );
        } catch (RuntimeException e) {
            throw new ContentSaveException();
        }
        return path;
    }

    @Override
    public void removeContent(String path) {
        try {
            s3Template.deleteObject(storageProperties.getBucketName(), path);
        } catch (RuntimeException e) {
            throw new ContentSaveException();
        }
    }

    @Override
    public InputStream loadContent(String path) {
        try {
            var load = s3Template.download(storageProperties.getBucketName(), path);
            return load.getInputStream();
        } catch (Exception e) {
            throw new ContentSaveException();
        }
    }
}
