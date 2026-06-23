package com.bookhub.bookservice.controllers;

import com.bookhub.bookservice.dtos.requests.BookCreateRequestDto;
import com.bookhub.bookservice.dtos.responses.BookResponseDto;
import com.bookhub.bookservice.mappers.BookMapper;
import com.bookhub.bookservice.security.GatewayUserDetails;
import com.bookhub.bookservice.services.BookService;
import com.bookhub.bookservice.validators.PDFValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookMapper bookMapper;
    private final BookService bookService;
    private final PDFValidator pdfValidator;

    @GetMapping("/{uuid}")
    public ResponseEntity<BookResponseDto> getBook(@PathVariable UUID uuid){
        var book = bookService.loadBookByUUID(uuid);
        return ResponseEntity.ok(bookMapper.toDto(book));
    }

    @PostMapping
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> createBook(@RequestBody @Valid BookCreateRequestDto bookCreateRequestDto,
                                           @AuthenticationPrincipal GatewayUserDetails userDetails) {
        var book = bookMapper.toBook(bookCreateRequestDto);
        bookService.createBook(book,userDetails.getUserId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PatchMapping(value = "/{uuid}/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> createBookContent(@PathVariable UUID uuid,
                                                  @RequestParam MultipartFile pdf,
                                                  @AuthenticationPrincipal GatewayUserDetails userDetails) throws IOException {
        pdfValidator.validateBookPDF(pdf);
        bookService.createBookContent(uuid,userDetails.getUserId(),pdf.getInputStream(),pdf.getSize());
        return ResponseEntity.noContent().build();
    }

}
