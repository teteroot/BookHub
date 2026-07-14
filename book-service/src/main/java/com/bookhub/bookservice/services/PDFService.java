package com.bookhub.bookservice.services;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface PDFService {

    List<Path> loadPages(InputStream content);
    Path collectBookFromPages(List<Path> pages);
    int getCountOfPages(InputStream content);
}
