package com.bookhub.bookservice.validators.impls;

import com.bookhub.bookservice.config.properties.CoverProperties;
import com.bookhub.bookservice.exceptions.extensions.TooManyPagesException;
import com.bookhub.bookservice.exceptions.extensions.UnsupportedCoverTypeException;
import com.bookhub.bookservice.services.ImageService;
import com.bookhub.bookservice.services.PDFService;
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
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
@Component
public class CoverValidatorImpl implements CoverValidator {

    private final Detector detector;
    private final CoverProperties coverProperties;
    private final PDFService pdfService;
    private final ImageService imageService;

    @Override
    public MediaType getCoverMediaType(MultipartFile cover) throws IOException {
        if (cover.isEmpty()) {
            throw new UnsupportedCoverTypeException();
        }
        try(InputStream stream = new BufferedInputStream(cover.getInputStream())) {
            var type = detector.detect(stream, new Metadata());
            return MediaType.parseMediaType(type.toString());
        }
    }

    @Override
    public void validateCoverMedia(MediaType mediaType, InputStream stream){

        if (!coverProperties.getSupportedTypes().contains(mediaType)) {
            throw new UnsupportedCoverTypeException(mediaType);
        }
        if (mediaType.equals(MediaType.APPLICATION_PDF)){
            if (pdfService.getCountOfPages(stream) != 1){
                throw new TooManyPagesException(1);
            }
        } else if (mediaType.getType().equals("image")) {
            imageService.validateFormat(new BufferedInputStream(stream),coverProperties.getTargetRatio(),coverProperties.getRatioTolerance());
        } else {
            throw new UnsupportedCoverTypeException(mediaType);
        }
    }
}
