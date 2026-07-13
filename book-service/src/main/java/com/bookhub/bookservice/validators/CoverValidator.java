package com.bookhub.bookservice.validators;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CoverValidator {
    MediaType getCoverMediaType(MultipartFile cover) throws IOException;

    void validateCoverMedia(MediaType mediaType, byte[] bytes);
}
