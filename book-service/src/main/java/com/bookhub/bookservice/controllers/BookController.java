package com.bookhub.bookservice.controllers;

import com.bookhub.bookservice.dtos.requests.BookCreateRequestDto;
import com.bookhub.bookservice.dtos.responses.BookResponseDto;
import com.bookhub.bookservice.mappers.BookMapper;
import com.bookhub.bookservice.security.GatewayUserDetails;
import com.bookhub.bookservice.services.BookOrchestrator;
import com.bookhub.bookservice.validators.CoverValidator;
import com.bookhub.bookservice.validators.PDFValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookMapper bookMapper;
    private final BookOrchestrator bookOrchestrator;
    private final CoverValidator coverValidator;
    private final PDFValidator pdfValidator;

    @GetMapping("/{uuid}/availability")
    public ResponseEntity<Void> checkBookAvailability(@PathVariable UUID uuid){
        bookOrchestrator.checkBookAvailability(uuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<BookResponseDto>> getAllBooks(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                             @RequestParam(required = false) UUID authorId,
                                                             @RequestParam Integer page){
        UUID id = userDetails != null ? userDetails.getUserId() : null;
        var books = bookOrchestrator.loadBooks(page, authorId, id);
        var dto = books.map((b) -> bookMapper.toDto(b,bookOrchestrator.getCountOfPages(b.getId())));
        return ResponseEntity.ok(new PagedModel<>(dto));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<BookResponseDto> getBook(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                   @PathVariable UUID uuid){
        UUID authorId = userDetails != null ? userDetails.getUserId() : null;
        var book = bookOrchestrator.loadBookByUUID(uuid,authorId);
        var countOfPages = bookOrchestrator.getCountOfPages(uuid);
        var dto = bookMapper.toDto(book,countOfPages);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadBooks(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                              @RequestParam List<UUID> bookIds) {
        UUID authorId = userDetails != null ? userDetails.getUserId() : null;
        var booksStream = bookOrchestrator.loadBooksArchiveStream(bookIds,authorId);
        StreamingResponseBody responseBody = outputStream -> {
            try (booksStream) {
                booksStream.transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=books.zip")
                .body(responseBody);
    }

    @GetMapping("/{uuid}/download")
    public ResponseEntity<StreamingResponseBody> downloadBook(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                 @PathVariable UUID uuid) {
        UUID authorId = userDetails != null ? userDetails.getUserId() : null;
        var bookStream = bookOrchestrator.loadBookStream(uuid,authorId);
        StreamingResponseBody responseBody = outputStream -> {
            try (bookStream) {
                bookStream.transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=book.pdf")
                .body(responseBody);
    }

    @GetMapping("/{uuid}/cover")
    public ResponseEntity<Resource> getBookCover(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                 @PathVariable UUID uuid){
        UUID authorId = userDetails != null ? userDetails.getUserId() : null;
        var bookCoverStream = bookOrchestrator.loadBookCoverStream(uuid, authorId);
        var contentType = bookOrchestrator.loadBookCoverContentType();
        return ResponseEntity.ok()
                .contentType(contentType)
                .body(new InputStreamResource(bookCoverStream));
    }

    @PatchMapping("/{uuid}/cover")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> updateBookCover(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                    @PathVariable UUID uuid,
                                                    @RequestParam MultipartFile cover) throws IOException {
        var type = coverValidator.getCoverMediaType(cover);
        try(InputStream coverStream = cover.getInputStream()){
            coverValidator.validateCoverMedia(type,coverStream);
        }
        try(InputStream coverStream = cover.getInputStream()) {
            bookOrchestrator.updateBookCover(uuid,userDetails.getUserId(),coverStream,cover.getSize(), type);
        }
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{uuid}/publish")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> publishBook(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                            @PathVariable UUID uuid){
        bookOrchestrator.publishBook(uuid, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{uuid}/draft")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> draftBook(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                          @PathVariable UUID uuid){
        bookOrchestrator.draftBook(uuid, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{uuid}/archive")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> archiveBook(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                          @PathVariable UUID uuid){
        bookOrchestrator.archiveBook(uuid, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> createBook(@RequestBody @Valid BookCreateRequestDto bookCreateRequestDto,
                                           @AuthenticationPrincipal GatewayUserDetails userDetails) {
        var book = bookMapper.toBook(bookCreateRequestDto);
        bookOrchestrator.createBook(book,userDetails.getUserId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID uuid,
                                           @AuthenticationPrincipal GatewayUserDetails userDetails) {
        bookOrchestrator.deleteBook(uuid,userDetails.getUserId());
        return ResponseEntity.ok().build();
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
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{uuid}/star")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<Void> starBook(@PathVariable UUID uuid) {
        bookOrchestrator.starBook(uuid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{uuid}/star")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<Void> removeStarFromBook(@PathVariable UUID uuid) {
        bookOrchestrator.removeStarFromBook(uuid);
        return ResponseEntity.noContent().build();
    }

}
