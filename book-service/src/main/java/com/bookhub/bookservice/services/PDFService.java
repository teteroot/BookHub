package com.bookhub.bookservice.services;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;

public interface PDFService {

    LinkedHashMap<InputStream,Long> loadPagesStreams(InputStream content);
    InputStream collectBookFromPages(List<InputStream> pages);
}
