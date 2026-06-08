package com.bookhub.bookservice.controllers;

import com.bookhub.bookservice.dtos.requests.BookCreateRequestDto;
import com.bookhub.bookservice.dtos.responses.BookResponseDto;
import com.bookhub.bookservice.mappers.BookMapper;
import com.bookhub.bookservice.security.GatewayUserDetails;
import com.bookhub.bookservice.services.BookManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookMapper bookMapper;
    private final BookManagementService bookManagementService;

    @GetMapping("/{uuid}")
    public ResponseEntity<BookResponseDto> getBook(@PathVariable UUID uuid){
        var book = bookManagementService.loadBookByUUID(uuid);
        return ResponseEntity.ok(bookMapper.toDto(book));
    }

    @PostMapping
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> createBook(@RequestBody @Valid BookCreateRequestDto bookCreateRequestDto,
                                           @AuthenticationPrincipal GatewayUserDetails userDetails) {
        var book = bookMapper.toBook(bookCreateRequestDto);
        bookManagementService.createBook(book,userDetails.getUserId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }



}
