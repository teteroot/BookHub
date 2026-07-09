package com.bookhub.bookservice.controllers;

import com.bookhub.bookservice.dtos.requests.BookCreateRequestDto;
import com.bookhub.bookservice.dtos.responses.BookResponseDto;
import com.bookhub.bookservice.mappers.BookMapper;
import com.bookhub.bookservice.security.GatewayUserDetails;
import com.bookhub.bookservice.services.BookOrchestrator;
import com.bookhub.bookservice.validators.PDFValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookMapper bookMapper;
    private final BookOrchestrator bookOrchestrator;
    private final PDFValidator pdfValidator;

    @GetMapping("/{uuid}")
    public ResponseEntity<BookResponseDto> getBook(@PathVariable UUID uuid){
        var book = bookOrchestrator.loadBookByUUID(uuid);
        var countOfPages = bookOrchestrator.getCountOfPages(uuid);
        var dto = bookMapper.toDto(book,countOfPages);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{uuid}/download")
    public ResponseEntity<Resource> downloadBook(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                 @PathVariable UUID uuid){
        UUID authorId = userDetails != null ? userDetails.getUserId() : null;
        var bookStream = bookOrchestrator.loadBookStream(authorId,uuid);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=book.pdf")
                .body(new InputStreamResource(bookStream));
    }

    @PostMapping
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> createBook(@RequestBody @Valid BookCreateRequestDto bookCreateRequestDto,
                                           @AuthenticationPrincipal GatewayUserDetails userDetails) {
        var book = bookMapper.toBook(bookCreateRequestDto);
        bookOrchestrator.createBook(book,userDetails.getUserId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping(value = "/{uuid}/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> createBookContent(@PathVariable UUID uuid,
                                                  @RequestParam MultipartFile pdf,
                                                  @AuthenticationPrincipal GatewayUserDetails userDetails) throws IOException {
        pdfValidator.validateBookPDF(pdf);
        try(InputStream content = pdf.getInputStream()) {
            bookOrchestrator.createBookContent(uuid,userDetails.getUserId(),content);
        }
        return ResponseEntity.noContent().build();
    }

}
