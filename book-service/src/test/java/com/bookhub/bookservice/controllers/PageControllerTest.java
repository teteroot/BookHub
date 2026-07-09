package com.bookhub.bookservice.controllers;

import com.bookhub.bookservice.config.SecurityConfig;
import com.bookhub.bookservice.exceptions.extensions.BookAccessDeniedException;
import com.bookhub.bookservice.exceptions.extensions.BookNotFoundException;
import com.bookhub.bookservice.exceptions.extensions.ContentLoadException;
import com.bookhub.bookservice.exceptions.extensions.PageNotFoundException;
import com.bookhub.bookservice.security.TestUserDetailsService;
import com.bookhub.bookservice.services.BookOrchestrator;
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

import java.io.InputStream;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PageController.class)
@Import({SecurityConfig.class, TestUserDetailsService.class})
class PageControllerTest {

    @MockitoBean
    private BookOrchestrator bookOrchestrator;

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
        when(bookOrchestrator.loadPageStreamByBookIdAndPageNumber(null,uuid,5))
                .thenReturn(InputStream.nullInputStream());
        mockMvc.perform(get("/api/v1/books/{uuid}/pages/{number}", uuid,5))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @WithUserDetails("READER")
    void testDownloadBookWithReaderPrincipal() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadPageStreamByBookIdAndPageNumber(testUserDetailsService.getUserId(),uuid,5))
                .thenReturn(InputStream.nullInputStream());
        mockMvc.perform(get("/api/v1/books/{uuid}/pages/{number}", uuid,5))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }


    @Test
    @WithUserDetails("READER")
    void testDownloadForbiddenDraftBook() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadPageStreamByBookIdAndPageNumber(testUserDetailsService.getUserId(),uuid,5))
                .thenThrow(new BookAccessDeniedException());

        mockMvc.perform(get("/api/v1/books/{uuid}/pages/{number}", uuid,5))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This is not your book"));
    }

    @Test
    @WithUserDetails("READER")
    void testDownloadNonExistBook() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadPageStreamByBookIdAndPageNumber(testUserDetailsService.getUserId(),uuid,5))
                .thenThrow(new BookNotFoundException());

        mockMvc.perform(get("/api/v1/books/{uuid}/pages/{number}", uuid,5))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found"));
    }

    @Test
    @WithUserDetails("READER")
    void testDownloadNonExistBookPage() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadPageStreamByBookIdAndPageNumber(testUserDetailsService.getUserId(),uuid,5))
                .thenThrow(new PageNotFoundException());

        mockMvc.perform(get("/api/v1/books/{uuid}/pages/{number}", uuid,5))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Page not found"));
    }

    @Test
    @WithUserDetails("READER")
    void testDownloadBookPageWithS3Error() throws Exception {
        var uuid = UUID.randomUUID();
        when(bookOrchestrator.loadPageStreamByBookIdAndPageNumber(testUserDetailsService.getUserId(),uuid,5))
                .thenThrow(new ContentLoadException());

        mockMvc.perform(get("/api/v1/books/{uuid}/pages/{number}", uuid,5))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to load book content"));
    }

}