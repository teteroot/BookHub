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
        var pageStream = bookOrchestrator.loadPageStreamByBookIdAndPageNumber(readerId,bookId, pageNumber);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pageStream));

    }

    @PatchMapping("/{pageNumber}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> updatePageByPageNumber(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                       @PathVariable UUID bookId,
                                                       @PathVariable Integer pageNumber,
                                                       @RequestBody MultipartFile pageContent) throws IOException {
        pdfValidator.validateBookPDF(pageContent);
        try(InputStream content = pageContent.getInputStream()) {
            bookOrchestrator.updatePageContent( userDetails.getUserId() ,bookId, pageNumber, content);
        }
        return ResponseEntity.ok().build();

    }
}
