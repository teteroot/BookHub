package com.bookhub.bookservice.services;

import org.springframework.http.MediaType;

import java.io.InputStream;
import java.io.OutputStream;

public interface ImageService {

    void validateFormat(InputStream imageStream, double targetRatio, double ratioTolerance);

    void convertToCommonFormat(InputStream imageStream, OutputStream outputStream);

    MediaType getCommonCoverType();

    int getPdfDpi();
}
