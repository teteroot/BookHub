package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.InternalServerErrorException;

public class PDFWorkerException extends InternalServerErrorException {
    public PDFWorkerException() {
        super("Error while processing PDF file");
    }
}
