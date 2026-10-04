package com.bookhub.bookservice.controllers;

import com.bookhub.bookservice.orchestrators.BookMediaOrchestrator;
import com.bookhub.bookservice.orchestrators.BookPageOrchestrator;
import com.bookhub.bookservice.security.GatewayUserDetails;
import com.bookhub.bookservice.validators.PDFValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
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

    private final BookPageOrchestrator bookPageOrchestrator;
    private final BookMediaOrchestrator bookMediaOrchestrator;
    private final PDFValidator pdfValidator;

    @GetMapping("/{pageNumber}")
    public ResponseEntity<Resource> getPageByPageNumber(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                        @PathVariable UUID bookId,
                                                        @PathVariable Integer pageNumber){
        UUID readerId = userDetails != null ? userDetails.getUserId() : null;
        var pageContent = bookMediaOrchestrator.loadPageStreamByBookIdAndPageNumber(bookId, readerId, pageNumber);
        return ResponseEntity.ok()
                .header("X-Page-Id", pageContent.pageId().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pageContent.stream()));

    }

    @PatchMapping(value = "/{pageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> updatePageByPageNumber(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                       @PathVariable UUID bookId,
                                                       @PathVariable UUID pageId,
                                                       @RequestParam MultipartFile pdf) throws IOException {
        pdfValidator.validateBookPDF(pdf);
        try(InputStream content = pdf.getInputStream()) {
            bookMediaOrchestrator.updatePageContent(bookId, userDetails.getUserId() , pageId, content);
        }
        return ResponseEntity.noContent().build();

    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> putNewPage(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                           @PathVariable UUID bookId,
                                           @RequestParam(required = false) Integer pageNumber,
                                           @RequestParam MultipartFile pdf) throws IOException {
        pdfValidator.validateBookPDF(pdf);
        try(InputStream content = pdf.getInputStream()) {
            bookMediaOrchestrator.createBookPage(bookId, userDetails.getUserId() , pageNumber, content);
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PatchMapping("/{pageId}/swap")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> swapPages(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                          @PathVariable UUID bookId,
                                          @PathVariable UUID pageId,
                                          @RequestParam UUID swapPageId){
        bookPageOrchestrator.swapBookPages(bookId, userDetails.getUserId(), pageId, swapPageId);
        return ResponseEntity.noContent().build();

    }

    @PatchMapping("/{pageId}/move")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> movePage(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                          @PathVariable UUID bookId,
                                          @PathVariable UUID pageId,
                                          @RequestParam Integer pageNumber){
        bookPageOrchestrator.moveBookPage(bookId, userDetails.getUserId(), pageId, pageNumber);
        return ResponseEntity.noContent().build();

    }

    @DeleteMapping("/{pageId}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> deletePage(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                           @PathVariable UUID bookId,
                                           @PathVariable UUID pageId){
        bookMediaOrchestrator.deleteBookPage(bookId, userDetails.getUserId(),pageId);
        return ResponseEntity.noContent().build();

    }
}
