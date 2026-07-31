package com.bookhub.bookservice.controllers;

import com.bookhub.bookservice.config.SecurityConfig;
import com.bookhub.bookservice.exceptions.extensions.*;
import com.bookhub.bookservice.security.TestUserDetailsService;
import com.bookhub.bookservice.services.BookOrchestrator;
import com.bookhub.bookservice.validators.PDFValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PageController.class)
@Import({SecurityConfig.class, TestUserDetailsService.class})
class PageControllerTest {

    @MockitoBean
    private BookOrchestrator bookOrchestrator;

    @MockitoBean
    private PDFValidator pdfValidator;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUserDetailsService testUserDetailsService;

    @Autowired
    private WebApplicationContext webApplicationContext;

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
    void testDownloadBookPageWithoutPrincipal_publicBookAllowed() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadPageStreamByBookIdAndPageNumber(uuid, null,5))
                .thenReturn(new BookOrchestrator.PageContent(InputStream.nullInputStream(), uuid));
        mockMvc.perform(get("/api/v1/books/{uuid}/pages/{number}", uuid,5))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @WithUserDetails("READER")
    void testDownloadBookWithReaderPrincipal() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadPageStreamByBookIdAndPageNumber(uuid, testUserDetailsService.getUserId(),5))
                .thenReturn(new BookOrchestrator.PageContent(InputStream.nullInputStream(), uuid));
        mockMvc.perform(get("/api/v1/books/{uuid}/pages/{number}", uuid,5))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }


    @Test
    @WithUserDetails("READER")
    void testDownloadForbiddenDraftBook() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadPageStreamByBookIdAndPageNumber(uuid, testUserDetailsService.getUserId() ,5))
                .thenThrow(new BookAccessDeniedException());

        mockMvc.perform(get("/api/v1/books/{uuid}/pages/{number}", uuid,5))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This is not your book"));
    }

    @Test
    @WithUserDetails("READER")
    void testDownloadNonExistBook() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadPageStreamByBookIdAndPageNumber(uuid, testUserDetailsService.getUserId(),5))
                .thenThrow(new BookNotFoundException());

        mockMvc.perform(get("/api/v1/books/{uuid}/pages/{number}", uuid,5))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found"));
    }

    @Test
    @WithUserDetails("READER")
    void testDownloadNonExistBookPage() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadPageStreamByBookIdAndPageNumber(uuid, testUserDetailsService.getUserId(),5))
                .thenThrow(new PageNotFoundException());

        mockMvc.perform(get("/api/v1/books/{uuid}/pages/{number}", uuid,5))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Page not found"));
    }

    @Test
    @WithUserDetails("READER")
    void testDownloadBookPageWithS3Error() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadPageStreamByBookIdAndPageNumber(uuid, testUserDetailsService.getUserId() ,5))
                .thenThrow(new ContentLoadException());

        mockMvc.perform(get("/api/v1/books/{uuid}/pages/{number}", uuid,5))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to load book content"));
    }

    @Test
    @WithUserDetails("AUTHOR")
    void testSuccessfulUpdatePageByPageNumber() throws Exception {
        var uuid = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "pdf", "page.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf".getBytes());

        mockMvc.perform(multipart(HttpMethod.PATCH,"/api/v1/books/{uuid}/pages/{number}", uuid,UUID.randomUUID())
                        .file(file))
                .andExpect(status().isOk());
    }


    @Test
    @WithUserDetails("AUTHOR")
    void testInvalidUpdatePageByPageNumber() throws Exception {
        var uuid = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "pdf", "page.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf".getBytes());
        doThrow(new PDFValidationException()).when(pdfValidator).validateBookPDF(file);
        mockMvc.perform(multipart(HttpMethod.PATCH,"/api/v1/books/{uuid}/pages/{number}", uuid,UUID.randomUUID())
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incorrect PDF file format"));
    }


    @Test
    @WithUserDetails("AUTHOR")
    void testUpdateNotAccessiblePageByPageNumber() throws Exception {
        var uuid = UUID.randomUUID();
        var pageId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "pdf", "page.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf".getBytes());
        doThrow(new BookAccessDeniedException())
                .when(bookOrchestrator).updatePageContent(eq(uuid), eq(testUserDetailsService.getUserId()), eq(pageId), any(InputStream.class));
        mockMvc.perform(multipart(HttpMethod.PATCH,"/api/v1/books/{uuid}/pages/{number}", uuid,pageId)
                        .file(file))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This is not your book"));
    }

    @Test
    @WithUserDetails("AUTHOR")
    void testUpdatePageWithNonDraftStatusByPageNumber() throws Exception {
        var uuid = UUID.randomUUID();
        var pageId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "pdf", "page.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf".getBytes());
        doThrow(new BookNotDraftingException())
                .when(bookOrchestrator).updatePageContent(eq(uuid), eq(testUserDetailsService.getUserId()), eq(pageId), any(InputStream.class));
        mockMvc.perform(multipart(HttpMethod.PATCH,"/api/v1/books/{uuid}/pages/{number}", uuid,pageId)
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Book status isn't \"Draft\""));
    }


    @Test
    @WithUserDetails("AUTHOR")
    void testUpdateTooManyPagesByPageNumber() throws Exception {
        var uuid = UUID.randomUUID();
        var pageId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "pdf", "page.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf".getBytes());
        doThrow(new TooManyPagesException(1))
                .when(bookOrchestrator).updatePageContent(eq(uuid), eq(testUserDetailsService.getUserId()), eq(pageId), any(InputStream.class));
        mockMvc.perform(multipart(HttpMethod.PATCH,"/api/v1/books/{uuid}/pages/{number}", uuid,pageId)
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Uploaded content must contain exactly 1 page"));
    }

    @Test
    @WithUserDetails("AUTHOR")
    void testUpdatePageByPageNumberWithServerError() throws Exception {
        var uuid = UUID.randomUUID();
        var pageId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "pdf", "page.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf".getBytes());
        doThrow(new ContentSaveException())
                .when(bookOrchestrator).updatePageContent(eq(uuid), eq(testUserDetailsService.getUserId()), eq(pageId), any(InputStream.class));
        mockMvc.perform(multipart(HttpMethod.PATCH,"/api/v1/books/{uuid}/pages/{number}", uuid,pageId)
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to save book content"));
    }
}