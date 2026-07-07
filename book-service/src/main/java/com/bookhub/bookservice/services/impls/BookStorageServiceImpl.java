package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.config.properties.StorageProperties;
import com.bookhub.bookservice.exceptions.extensions.ContentSaveException;
import com.bookhub.bookservice.services.BookStorageService;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BookStorageServiceImpl implements BookStorageService {

    private final S3Template s3Template;
    private final StorageProperties storageProperties;
    private final S3Client s3Client;

    @Override
    public String createPageContent(UUID bookId, UUID pageId, InputStream content, Long size) {
        var path = "%s%s/%s%s".formatted(
                storageProperties.getDestination().formatted(bookId),
                storageProperties.getPageDestination(),
                pageId,
                storageProperties.getFileExtension()
        );
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
    public String createBookContent(UUID bookId,InputStream content, Long size) {
        var path = "%s/content%s".formatted(
                storageProperties.getDestination().formatted(bookId),
                storageProperties.getFileExtension()
        );
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
    public InputStream loadContent(String path) {
        try {
            var load = s3Template.download(storageProperties.getBucketName(), path);
            try(InputStream inputStream = load.getInputStream()) {
                return new ByteArrayInputStream(inputStream.readAllBytes());
            }
        } catch (Exception e) {
            throw new ContentSaveException();
        }
    }

    @Override
    public void removeBookContent(UUID bookId) {

        var path = "%s/".formatted(storageProperties.getDestination().formatted(bookId));
        var toDelete = s3Template.listObjects(storageProperties.getBucketName(),path);
        try {
            var dor = DeleteObjectsRequest.builder()
                    .bucket(storageProperties.getBucketName())
                    .delete(Delete.builder().objects(
                            toDelete.stream().map((s3Object) -> ObjectIdentifier.builder().key(s3Object.getFilename()).build()).toList()
                    ).build())
                    .build();

            s3Client.deleteObjects(dor);
        } catch (Exception e) {
            throw new ContentSaveException();
        }
    }
}
