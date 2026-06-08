package com.bookhub.bookservice.controllers;

import com.bookhub.bookservice.dtos.requests.BookCreateRequestDto;
import com.bookhub.bookservice.mappers.BookMapper;
import com.bookhub.bookservice.security.GatewayUserDetails;
import com.bookhub.bookservice.services.BookManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookMapper bookMapper;
    private final BookManagementService bookManagementService;

    @PostMapping
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<Void> createBook(@RequestBody BookCreateRequestDto bookCreateRequestDto,
                                           @AuthenticationPrincipal GatewayUserDetails userDetails) {
        var book = bookMapper.toBook(bookCreateRequestDto);
        bookManagementService.createBook(book,userDetails.getUserId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }



}
