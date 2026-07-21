package com.bookhub.bookservice.controllers;

import com.bookhub.bookservice.security.GatewayUserDetails;
import com.bookhub.bookservice.services.BookOrchestrator;
import com.bookhub.bookservice.validators.PDFValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/books/{bookId}/pages")
public class PageController {

    private final BookOrchestrator bookOrchestrator;
    private final PDFValidator pdfValidator;

    @GetMapping("/{pageNumber}")
    public ResponseEntity<Resource> getPageByPageNumber(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                        @PathVariable UUID bookId,
                                                        @PathVariable Integer pageNumber){
        UUID readerId = userDetails != null ? userDetails.getUserId() : null;
        var pageContent = bookOrchestrator.loadPageStreamByBookIdAndPageNumber(bookId, readerId, pageNumber);
        return ResponseEntity.ok()
                .header("X-Page-Id", pageContent.pageId().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pageContent.stream()));

    }

    @PatchMapping("/{pageId}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> updatePageByPageNumber(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                       @PathVariable UUID bookId,
                                                       @PathVariable UUID pageId,
                                                       @RequestParam MultipartFile pdf) throws IOException {
        pdfValidator.validateBookPDF(pdf);
        try(InputStream content = pdf.getInputStream()) {
            bookOrchestrator.updatePageContent(bookId, userDetails.getUserId() , pageId, content);
        }
        return ResponseEntity.ok().build();

    }

    @PostMapping
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> putNewPage(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                              @PathVariable UUID bookId,
                                              @RequestParam(required = false) Integer pageNumber,
                                              @RequestParam MultipartFile pdf) throws IOException {
        pdfValidator.validateBookPDF(pdf);
        try(InputStream content = pdf.getInputStream()) {
            bookOrchestrator.createBookPage(bookId, userDetails.getUserId() , pageNumber, content);
        }
        return ResponseEntity.ok().build();

    }
}
