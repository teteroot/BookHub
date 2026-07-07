package com.bookhub.bookservice.services;

import java.io.InputStream;
import java.util.UUID;

public interface BookStorageService {

    String createPageContent(UUID bookId,UUID pageId, InputStream content, Long size);

    InputStream loadContent(String path);

    void removeBookContent(UUID bookId);

    String createBookContent(UUID bookId,InputStream content, Long size);
}
