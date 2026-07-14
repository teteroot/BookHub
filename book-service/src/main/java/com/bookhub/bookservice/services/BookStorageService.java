package com.bookhub.bookservice.services;

import java.io.InputStream;
import java.util.UUID;

public interface BookStorageService {

    String createPageContent(UUID bookId,UUID pageId, InputStream content, Long size);

    InputStream loadContent(String path);

    void removeBook(UUID bookId);

    String createBookContent(UUID bookId,InputStream content, Long size);

    void updatePageContent(String s3FilePath, InputStream pageStream, long pageSize);

    String createBookCover(UUID bookId, String extension, String contentType, InputStream is, long size);

    void removeBookCover(String coverPath);
}
