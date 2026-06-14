package com.bookhub.bookservice.validators.impls;

import com.bookhub.bookservice.validators.PDFValidator;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class PDFValidatorImpl implements PDFValidator {
    @Override
    public void validateBookPDF(MultipartFile pdf) {
        //TODO validate
    }
}
