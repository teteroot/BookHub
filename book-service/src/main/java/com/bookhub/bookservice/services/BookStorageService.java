package com.bookhub.bookservice.services;

import java.io.InputStream;
import java.util.UUID;

public interface BookStorageService {

    String updateContent(UUID bookId, InputStream content, Long size);
}
