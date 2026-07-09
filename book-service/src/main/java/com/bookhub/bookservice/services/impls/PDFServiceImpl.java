package com.bookhub.bookservice.services.impls;

import com.bookhub.bookservice.services.FileTempService;
import com.bookhub.bookservice.services.PDFService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PDFServiceImpl implements PDFService {

    private final FileTempService fileTempService;

    @Override
    public List<Path> loadPages(InputStream content) {
        List<Path> pages = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(RandomAccessReadBuffer.createBufferFromStream(content))){
            for (PDPage page : document.getPages()) {
                try(PDDocument singlePage = new PDDocument()) {
                    singlePage.importPage(page);
                    pages.add(fileTempService.writeToTempFile("page-", ".pdf", singlePage::save));
                }
            }
        } catch (IOException e) {
            fileTempService.deleteQuietly(pages);
            throw new RuntimeException(e);
        }
        return pages;
    }

    @Override
    public Path collectBookFromPages(List<Path> pages) {
        return fileTempService.writeToTempFile("merged-book",".pdf", fos -> {
            PDFMergerUtility mergerUtility = new PDFMergerUtility();
            mergerUtility.setDestinationStream(fos);
            for (Path page : pages) {
                mergerUtility.addSource(page.toFile());
            }
            mergerUtility.mergeDocuments(MemoryUsageSetting.setupMixed(10 * 1024 * 1024).streamCache);
        });
    }
}
