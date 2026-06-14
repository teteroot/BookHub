package com.bookhub.bookservice.validators;

import org.springframework.web.multipart.MultipartFile;

public interface PDFValidator {


    void validateBookPDF(MultipartFile pdf);
}
