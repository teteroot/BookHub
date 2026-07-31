package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.BadRequestException;

public class PDFValidationException extends BadRequestException {
    public PDFValidationException() {
        super("Incorrect PDF file format");
    }
}
