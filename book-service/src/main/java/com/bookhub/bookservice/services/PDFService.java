package com.bookhub.bookservice.services;

import java.io.IOException;
import java.io.InputStream;

public interface PDFService {

    Integer countOfPages(InputStream stream) throws IOException;
}
