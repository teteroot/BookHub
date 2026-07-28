package com.bookhub.profileservice.controllers;

import com.bookhub.profileservice.config.SecurityConfig;
import com.bookhub.profileservice.exceptions.extensions.PersonAlreadyInFavoritesException;
import com.bookhub.profileservice.exceptions.extensions.PersonNotFoundException;
import com.bookhub.profileservice.exceptions.extensions.SelfRequestException;
import com.bookhub.profileservice.mappers.PersonMapper;
import com.bookhub.profileservice.security.TestUserDetailsService;
import com.bookhub.profileservice.services.FavoritesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FavoritesController.class)
@Import({SecurityConfig.class, TestUserDetailsService.class})
class FavoritesControllerTest {

    @MockitoBean
    private PersonMapper personMapper;

    @MockitoBean
    private FavoritesService favoritesService;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private TestUserDetailsService testUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @Value("${security.origin.gateway.secret}")
    private String gatewaySecret;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(get("/").header("X-Gateway-Secret", gatewaySecret))
                .build();
    }

    @Test
    @WithUserDetails
    void testGetMyFavoritesAuthors() throws Exception {
        when(favoritesService.loadFavoriteAuthors(any())).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/persons/favorites/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithUserDetails
    void testSuccessfulAddToFavorites() throws Exception {
        UUID uuid = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/persons/favorites/authors/{uuid}", uuid))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails
    void testAddToFavoritesAlreadyFavoritePerson() throws Exception {
        UUID uuid = UUID.randomUUID();
        doThrow(new PersonAlreadyInFavoritesException())
                .when(favoritesService).addToFavoriteAuthors(testUserDetailsService.getUserId(),uuid);
        mockMvc.perform(post("/api/v1/persons/favorites/authors/{uuid}", uuid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Person already in favorites"));
    }

    @Test
    @WithUserDetails
    void testAddToFavoritesNonExistPerson() throws Exception {
        UUID uuid = UUID.randomUUID();
        doThrow(new PersonNotFoundException())
                .when(favoritesService).addToFavoriteAuthors(testUserDetailsService.getUserId(),uuid);
        mockMvc.perform(post("/api/v1/persons/favorites/authors/{uuid}", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person not found"));
    }

    @Test
    @WithUserDetails
    void testAddToFavoritesYourself() throws Exception {
        UUID uuid = testUserDetailsService.getUserId();
        doThrow(new SelfRequestException())
                .when(favoritesService).addToFavoriteAuthors(uuid,uuid);
        mockMvc.perform(post("/api/v1/persons/favorites/authors/{uuid}", uuid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This is you"));
    }

    @Test
    @WithUserDetails
    void testSuccessfulRemoveFromFavorites() throws Exception {
        UUID uuid = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/persons/favorites/authors/{uuid}", uuid))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails
    void testRemoveFromFavoritesNonExistOrNonFavoritePerson() throws Exception {
        UUID uuid = UUID.randomUUID();
        doThrow(new PersonNotFoundException())
                .when(favoritesService).removeFromFavoriteAuthors(testUserDetailsService.getUserId(),uuid);
        mockMvc.perform(delete("/api/v1/persons/favorites/authors/{uuid}", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person not found"));
    }

    @Test
    @WithUserDetails
    void testRemoveFromFavoritesYourself() throws Exception {
        UUID uuid = testUserDetailsService.getUserId();
        doThrow(new SelfRequestException())
                .when(favoritesService).removeFromFavoriteAuthors(uuid,uuid);
        mockMvc.perform(delete("/api/v1/persons/favorites/authors/{uuid}", uuid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This is you"));
    }
}