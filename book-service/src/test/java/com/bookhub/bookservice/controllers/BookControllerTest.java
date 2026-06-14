package com.bookhub.bookservice.controllers;


import com.bookhub.bookservice.config.SecurityConfig;
import com.bookhub.bookservice.dtos.requests.BookCreateRequestDto;
import com.bookhub.bookservice.dtos.responses.BookResponseDto;
import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.BookAlreadyExistException;
import com.bookhub.bookservice.exceptions.extensions.BookNotFoundException;
import com.bookhub.bookservice.mappers.BookMapper;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.security.TestUserDetailsService;
import com.bookhub.bookservice.services.BookService;
import com.bookhub.bookservice.validators.PDFValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookController.class)
@Import({SecurityConfig.class, TestUserDetailsService.class})
class BookControllerTest {


    @MockitoBean
    private BookMapper bookMapper;

    @MockitoBean PDFValidator pdfValidator;

    @MockitoBean
    private BookService bookService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${security.origin.gateway.secret}")
    private String gatewaySecret;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(get("/").header("X-Gateway-Secret", gatewaySecret))
                .apply(springSecurity())
                .build();
    }


    @Test
    void testCreateBookWithoutPrincipal() throws Exception {
        BookCreateRequestDto dto =  new BookCreateRequestDto(
                "Title", "Description", 18
        );
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());

    }

    @Test
    @WithUserDetails("READER")
    void testCreateBookWithReaderUserDetails() throws Exception {
        BookCreateRequestDto dto =  new BookCreateRequestDto(
                "Title", "Description", 18
        );
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithUserDetails("AUTHOR")
    void testSuccessfulCreateBook() throws Exception {
        BookCreateRequestDto dto =  new BookCreateRequestDto(
                "Title", "Description", 18
        );
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

    }

    @Test
    @WithUserDetails("AUTHOR")
    void testCreateBookWithIncorrectData() throws Exception {
        BookCreateRequestDto dto =  new BookCreateRequestDto(
                "Title", "Description", -1
        );
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Age limit cannot be negative; "));

    }

    @Test
    @WithUserDetails("AUTHOR")
    void testCreateAlreadyExistBook() throws Exception {
        BookCreateRequestDto dto =  new BookCreateRequestDto(
                "Title", "Description", 5
        );

        doThrow(new BookAlreadyExistException("book_title"))
                .when(bookService).createBook(any(), any());
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Book with title book_title is already exist"));

    }

    @Test
    void testSuccessfulGetBook() throws Exception {
        var uuid = UUID.randomUUID();
        var book = Book.builder().id(uuid).build();
        when(bookService.loadBookByUUID(uuid))
                .thenReturn(book);
        when(bookMapper.toDto(book))
                .thenReturn(new BookResponseDto(
                        uuid,"","",5,UUID.randomUUID(), BookStatus.DRAFT, Instant.now(), 0, "", ""
                ));
        mockMvc.perform(get("/api/v1/books/{uuid}",uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(String.valueOf(uuid)));

    }

    @Test
    void testGetNonExistBook() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookService.loadBookByUUID(uuid))
                .thenThrow(new BookNotFoundException());
        mockMvc.perform(get("/api/v1/books/{uuid}", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found"));

    }

}