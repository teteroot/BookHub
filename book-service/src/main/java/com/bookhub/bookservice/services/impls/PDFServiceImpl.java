package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.services.PDFService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class PDFServiceImpl implements PDFService {

    @Override
    public LinkedHashMap<InputStream, Long> loadPagesStreams(InputStream content) {
        LinkedHashMap<InputStream, Long> pages = new LinkedHashMap<>();

        try (PDDocument document = Loader.loadPDF(RandomAccessReadBuffer.createBufferFromStream(content))){
            document.getPages().forEach((page) -> {
                try(PDDocument singlePage = new PDDocument()) {
                    singlePage.importPage(page);
                    var byteArrayStream = new ByteArrayOutputStream();
                    singlePage.save(byteArrayStream);
                    byte[] pageBytes = byteArrayStream.toByteArray();
                    pages.put(new ByteArrayInputStream(pageBytes), (long) pageBytes.length);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pages;
    }

    @Override
    public InputStream collectBookFromPages(List<InputStream> pages) {
        try {
            PDFMergerUtility mergerUtility = new PDFMergerUtility();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            mergerUtility.setDestinationStream(byteArrayOutputStream);
            for (InputStream page : pages) {
                mergerUtility.addSource(RandomAccessReadBuffer.createBufferFromStream(page));
            }
            mergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly().streamCache);
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
