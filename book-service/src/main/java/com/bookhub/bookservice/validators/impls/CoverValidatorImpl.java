package com.bookhub.bookservice.validators.impls;

import com.bookhub.bookservice.config.properties.CoverProperties;
import com.bookhub.bookservice.exceptions.extensions.PDFValidationException;
import com.bookhub.bookservice.exceptions.extensions.UnsupportedCoverTypeException;
import com.bookhub.bookservice.validators.CoverValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class CoverValidatorImpl implements CoverValidator {

    private final Detector detector;
    private final CoverProperties coverProperties;

    @Override
    public MediaType getCoverMediaType(MultipartFile cover) throws IOException {
        if (cover.isEmpty()) {
            throw new PDFValidationException();
        }
        var type = detector.detect(
                new BufferedInputStream(cover.getInputStream()), new Metadata()
        );
        return MediaType.parseMediaType(type.toString());
    }

    @Override
    public void validateCoverMedia(MediaType mediaType, byte[] bytes){

        if (!coverProperties.getSupportedTypes().contains(mediaType)) {
            throw new UnsupportedCoverTypeException(mediaType);
        }
        if (mediaType.equals(MediaType.APPLICATION_PDF)){
            //pdf validation logic
        } else if (mediaType.getType().equals("image")) {
            //image validation logic
        } else {
            throw new UnsupportedCoverTypeException(mediaType);
        }
    }
}
