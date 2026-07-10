package com.bookhub.bookservice.controllers;


import com.bookhub.bookservice.config.SecurityConfig;
import com.bookhub.bookservice.dtos.requests.BookCreateRequestDto;
import com.bookhub.bookservice.dtos.responses.BookResponseDto;
import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.extensions.*;
import com.bookhub.bookservice.mappers.BookMapper;
import com.bookhub.bookservice.models.Book;
import com.bookhub.bookservice.security.TestUserDetailsService;
import com.bookhub.bookservice.services.BookOrchestrator;
import com.bookhub.bookservice.validators.PDFValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookController.class)
@Import({SecurityConfig.class, TestUserDetailsService.class})
class BookControllerTest {

    @MockitoBean
    private BookMapper bookMapper;

    @MockitoBean
    private PDFValidator pdfValidator;

    @MockitoBean
    private BookOrchestrator bookOrchestrator;

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
        BookCreateRequestDto dto = new BookCreateRequestDto("Title", "Description", 18);
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("READER")
    void testCreateBookWithReaderUserDetails() throws Exception {
        BookCreateRequestDto dto = new BookCreateRequestDto("Title", "Description", 18);
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("AUTHOR")
    void testSuccessfulCreateBook() throws Exception {
        BookCreateRequestDto dto = new BookCreateRequestDto("Title", "Description", 18);
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithUserDetails("AUTHOR")
    void testCreateBookWithIncorrectData() throws Exception {
        BookCreateRequestDto dto = new BookCreateRequestDto("Title", "Description", -1);
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Age limit cannot be negative; "));
    }

    @Test
    @WithUserDetails("AUTHOR")
    void testCreateAlreadyExistBook() throws Exception {
        BookCreateRequestDto dto = new BookCreateRequestDto("Title", "Description", 5);

        doThrow(new BookAlreadyExistException("book_title"))
                .when(bookOrchestrator).createBook(any(), any());
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
        when(bookOrchestrator.loadBookByUUID(uuid)).thenReturn(book);
        when(bookMapper.toDto(book, 0))
                .thenReturn(new BookResponseDto(
                        uuid, "", "", 5, UUID.randomUUID(), BookStatus.DRAFT, Instant.now(), 0, "", ""
                ));
        mockMvc.perform(get("/api/v1/books/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(String.valueOf(uuid)));
    }

    @Test
    void testGetNonExistBook() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadBookByUUID(uuid)).thenThrow(new BookNotFoundException());
        mockMvc.perform(get("/api/v1/books/{uuid}", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found"));
    }

    @Test
    void testDownloadBookWithoutPrincipal_publicBookAllowed() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadBookStream(isNull(), eq(uuid)))
                .thenReturn(InputStream.nullInputStream());

        mockMvc.perform(get("/api/v1/books/{uuid}/download", uuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=book.pdf"));
    }

    @Test
    @WithUserDetails("READER")
    void testDownloadBookWithReaderPrincipal() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadBookStream(any(), eq(uuid)))
                .thenReturn(InputStream.nullInputStream());

        mockMvc.perform(get("/api/v1/books/{uuid}/download", uuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @WithUserDetails("AUTHOR")
    void testDownloadOwnDraftBook() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadBookStream(any(), eq(uuid)))
                .thenReturn(InputStream.nullInputStream());

        mockMvc.perform(get("/api/v1/books/{uuid}/download", uuid))
                .andExpect(status().isOk());
    }

    @Test
    void testDownloadForbiddenDraftBook_anonymousAccess() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadBookStream(isNull(), eq(uuid)))
                .thenThrow(new BookAccessDeniedException());

        mockMvc.perform(get("/api/v1/books/{uuid}/download", uuid))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This is not your book"));
    }

    @Test
    void testDownloadNonExistBook() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadBookStream(isNull(), eq(uuid)))
                .thenThrow(new BookNotFoundException());

        mockMvc.perform(get("/api/v1/books/{uuid}/download", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found"));
    }

    @Test
    void testDownloadBookWithNoContentYet() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadBookStream(isNull(), eq(uuid)))
                .thenThrow(new BookContentNotFoundException());

        mockMvc.perform(get("/api/v1/books/{uuid}/download", uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateBookContentWithoutPrincipal() throws Exception {
        var uuid = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "pdf", "book.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());

        mockMvc.perform(multipart("/api/v1/books/{uuid}/content", uuid)
                        .file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("READER")
    void testCreateBookContentWithReaderUserDetails() throws Exception {
        var uuid = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "pdf", "book.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());

        mockMvc.perform(multipart("/api/v1/books/{uuid}/content", uuid)
                        .file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("AUTHOR")
    void testSuccessfulCreateBookContent() throws Exception {
        var uuid = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "pdf", "book.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());

        mockMvc.perform(multipart("/api/v1/books/{uuid}/content", uuid)
                        .file(file))
                .andExpect(status().isOk());

        verify(pdfValidator).validateBookPDF(any());
        verify(bookOrchestrator).createBookContent(eq(uuid), any(), any());
    }

    @Test
    @WithUserDetails("AUTHOR")
    void testCreateBookContent_invalidPdf_throwsValidationException() throws Exception {
        var uuid = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "pdf", "book.txt", MediaType.TEXT_PLAIN_VALUE, "not a pdf".getBytes());

        doThrow(new PDFValidationException()).when(pdfValidator).validateBookPDF(any());

        mockMvc.perform(multipart("/api/v1/books/{uuid}/content", uuid)
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incorrect PDF file format"));

        verify(bookOrchestrator, never()).createBookContent(any(), any(), any());
    }

    @Test
    @WithUserDetails("AUTHOR")
    void testCreateBookContent_bookNotFound() throws Exception {
        var uuid = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "pdf", "book.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());

        doThrow(new BookNotFoundException())
                .when(bookOrchestrator).createBookContent(eq(uuid), any(), any());

        mockMvc.perform(multipart("/api/v1/books/{uuid}/content", uuid)
                        .file(file))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found"));
    }

    @Test
    @WithUserDetails("AUTHOR")
    void testCreateBookContent_notOwner_throwsAccessDenied() throws Exception {
        var uuid = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "pdf", "book.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());

        doThrow(new BookAccessDeniedException())
                .when(bookOrchestrator).createBookContent(eq(uuid), any(), any());

        mockMvc.perform(multipart("/api/v1/books/{uuid}/content", uuid)
                        .file(file))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This is not your book"));
    }

    @Test
    @WithUserDetails("AUTHOR")
    void testCreateBookContent_contentAlreadyExists() throws Exception {
        var uuid = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "pdf", "book.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());

        doThrow(new BookContentAlreadyExistException())
                .when(bookOrchestrator).createBookContent(eq(uuid), any(), any());

        mockMvc.perform(multipart("/api/v1/books/{uuid}/content", uuid)
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Book content already exists"));
    }

    @Test
    @WithUserDetails("AUTHOR")
    void testCreateBookContent_storageFailure_returnsInternalServerError() throws Exception {
        var uuid = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "pdf", "book.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());

        doThrow(new ContentSaveException())
                .when(bookOrchestrator).createBookContent(eq(uuid), any(), any());

        mockMvc.perform(multipart("/api/v1/books/{uuid}/content", uuid)
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to save book content"));
    }

    @Test
    @WithUserDetails("AUTHOR")
    void testCreateBookContent_optimisticLock_returnsConflict() throws Exception {
        var uuid = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "pdf", "book.pdf", MediaType.APPLICATION_PDF_VALUE, "content".getBytes());

        doThrow(new BookConcurrentModificationException())
                .when(bookOrchestrator).createBookContent(eq(uuid), any(), any());

        mockMvc.perform(multipart("/api/v1/books/{uuid}/content", uuid)
                        .file(file))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Book is uploading now"));
    }

    @Test
    @WithUserDetails("AUTHOR")
    void testCreateBookContent_missingFilePart_returnsBadRequest() throws Exception {
        var uuid = UUID.randomUUID();

        mockMvc.perform(multipart("/api/v1/books/{uuid}/content", uuid))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pdfValidator);
        verify(bookOrchestrator, never()).createBookContent(any(), any(), any());
    }
}