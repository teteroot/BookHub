package com.bookhub.bookservice.validators;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PDFValidator {


    void validateBookPDF(MultipartFile pdf) throws IOException;
}
