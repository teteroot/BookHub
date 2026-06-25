package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.services.PDFService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class PDFServiceImpl implements PDFService {

    @Override
    public Integer countOfPages(InputStream stream){
        try (PDDocument document = Loader.loadPDF(RandomAccessReadBuffer.createBufferFromStream(stream))){
            return document.getNumberOfPages();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
