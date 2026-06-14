package com.bookhub.bookservice.validators.impls;

import com.bookhub.bookservice.exceptions.extensions.PDFValidationException;
import com.bookhub.bookservice.validators.PDFValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class PDFValidatorImpl implements PDFValidator {

    private final Detector detector;

    @Override
    public void validateBookPDF(MultipartFile pdf) throws IOException {
        if (pdf.isEmpty() || !Objects.equals(pdf.getContentType(), "application/pdf")) {
            throw new PDFValidationException();
        }
        MediaType type =  detector.detect(new BufferedInputStream(pdf.getInputStream()), new Metadata());

        if (!type.equals(MediaType.application("pdf"))){
            throw new PDFValidationException();
        }
    }
}
