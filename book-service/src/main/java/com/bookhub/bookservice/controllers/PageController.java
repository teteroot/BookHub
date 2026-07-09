package com.bookhub.bookservice.controllers;

import com.bookhub.bookservice.security.GatewayUserDetails;
import com.bookhub.bookservice.services.BookOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/books/{bookId}/pages")
public class PageController {

    private final BookOrchestrator bookOrchestrator;

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
}
