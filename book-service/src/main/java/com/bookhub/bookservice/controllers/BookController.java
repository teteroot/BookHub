package com.bookhub.bookservice.controllers;

import com.bookhub.bookservice.dtos.requests.BookCreateRequestDto;
import com.bookhub.bookservice.dtos.responses.BookResponseDto;
import com.bookhub.bookservice.mappers.BookMapper;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.orchestrators.BookLifecycleOrchestrator;
import com.bookhub.bookservice.orchestrators.BookMediaOrchestrator;
import com.bookhub.bookservice.orchestrators.BookPageOrchestrator;
import com.bookhub.bookservice.orchestrators.BookQueryOrchestrator;
import com.bookhub.bookservice.security.GatewayUserDetails;
import com.bookhub.bookservice.validators.CoverValidator;
import com.bookhub.bookservice.validators.PDFValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookMapper bookMapper;
    private final CoverValidator coverValidator;
    private final PDFValidator pdfValidator;

    private final BookLifecycleOrchestrator bookLifecycleOrchestrator;
    private final BookMediaOrchestrator bookMediaOrchestrator;
    private final BookQueryOrchestrator bookQueryOrchestrator;
    private final BookPageOrchestrator bookPageOrchestrator;

    @GetMapping("/{uuid}/availability")
    public ResponseEntity<Void> checkBookAvailability(@PathVariable UUID uuid){
        bookQueryOrchestrator.checkBookAvailability(uuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<BookResponseDto>> getAllBooks(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                             @RequestParam(required = false) UUID authorId,
                                                             @RequestParam(required = false) String query,
                                                             @RequestParam Integer page){
        UUID id = userDetails != null ? userDetails.getUserId() : null;
        var books = bookQueryOrchestrator.loadBooks(page, query, authorId, id);
        var counts = bookPageOrchestrator.getAllCountOfPages(books.map(Book::getId).toList());
        var dto = books.map((b) -> bookMapper.toDto(b,counts.getOrDefault(b.getId(), 0)));
        return ResponseEntity.ok(new PagedModel<>(dto));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<BookResponseDto> getBook(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                   @PathVariable UUID uuid){
        UUID authorId = userDetails != null ? userDetails.getUserId() : null;
        var book = bookQueryOrchestrator.loadBookByUUID(uuid,authorId);
        var countOfPages = bookPageOrchestrator.getCountOfPages(uuid);
        var dto = bookMapper.toDto(book,countOfPages);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadBooks(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                                              @RequestParam List<UUID> bookIds) {
        UUID authorId = userDetails != null ? userDetails.getUserId() : null;
        var booksStream = bookMediaOrchestrator.loadBooksArchiveStream(bookIds,authorId);
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
        var bookStream = bookMediaOrchestrator.loadBookStream(uuid,authorId);
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
        var bookCoverStream = bookMediaOrchestrator.loadBookCoverStream(uuid, authorId);
        var contentType = bookMediaOrchestrator.loadBookCoverContentType();
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
            bookMediaOrchestrator.updateBookCover(uuid,userDetails.getUserId(),coverStream,cover.getSize(), type);
        }
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/{uuid}/publish")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> publishBook(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                            @PathVariable UUID uuid){
        bookLifecycleOrchestrator.publishBook(uuid, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{uuid}/draft")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> draftBook(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                          @PathVariable UUID uuid){
        bookLifecycleOrchestrator.draftBook(uuid, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{uuid}/archive")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> archiveBook(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                          @PathVariable UUID uuid){
        bookLifecycleOrchestrator.archiveBook(uuid, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> createBook(@RequestBody @Valid BookCreateRequestDto bookCreateRequestDto,
                                           @AuthenticationPrincipal GatewayUserDetails userDetails) {
        var book = bookMapper.toBook(bookCreateRequestDto);
        var bookId = bookLifecycleOrchestrator.createBook(book,userDetails.getUserId());
        return ResponseEntity.created(URI.create("/books/" + bookId)).build();
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID uuid,
                                           @AuthenticationPrincipal GatewayUserDetails userDetails) {
        bookLifecycleOrchestrator.deleteBook(uuid,userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{uuid}/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> createBookContent(@PathVariable UUID uuid,
                                                  @RequestParam MultipartFile pdf,
                                                  @AuthenticationPrincipal GatewayUserDetails userDetails) throws IOException {
        pdfValidator.validateBookPDF(pdf);
        try(InputStream content = pdf.getInputStream()) {
            bookMediaOrchestrator.createBookContent(uuid,userDetails.getUserId(),content);
        }
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{uuid}/star")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<Void> starBook(@PathVariable UUID uuid) {
        bookQueryOrchestrator.starBook(uuid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{uuid}/star")
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<Void> removeStarFromBook(@PathVariable UUID uuid) {
        bookQueryOrchestrator.removeStarFromBook(uuid);
        return ResponseEntity.noContent().build();
    }

}
