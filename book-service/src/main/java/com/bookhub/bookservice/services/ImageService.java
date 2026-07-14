package com.bookhub.bookservice.services;

import java.io.InputStream;

public interface ImageService {

    void validateFormat(InputStream imageStream, double targetRatio, double ratioTolerance);

}
