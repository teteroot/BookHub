package com.bookhub.profileservice.controllers;

import com.bookhub.profileservice.config.SecurityConfig;
import com.bookhub.profileservice.dtos.requests.PersonCreateRequestDto;
import com.bookhub.profileservice.dtos.requests.PersonUpdateRequestDto;
import com.bookhub.profileservice.dtos.responses.BiographyResponseDto;
import com.bookhub.profileservice.dtos.responses.PersonResponseDto;
import com.bookhub.profileservice.enums.UserRole;
import com.bookhub.profileservice.exceptions.extensions.BiographyNotFoundException;
import com.bookhub.profileservice.exceptions.extensions.PersonAlreadyInFavoritesException;
import com.bookhub.profileservice.exceptions.extensions.PersonNotFoundException;
import com.bookhub.profileservice.exceptions.extensions.SelfRequestException;
import com.bookhub.profileservice.mappers.PersonMapper;
import com.bookhub.profileservice.models.Person;
import com.bookhub.profileservice.services.PersonService;
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
import com.bookhub.profileservice.security.TestUserDetailsService;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PersonController.class)
@Import({SecurityConfig.class, TestUserDetailsService.class})
class PersonControllerTest {

    @MockitoBean
    private PersonMapper personMapper;

    @MockitoBean
    private PersonService personService;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private TestUserDetailsService testUserDetailsService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${security.origin.gateway.secret}")
    private String gatewaySecret;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(get("/").header("X-Gateway-Secret", gatewaySecret))
                .build();
    }



    @Test
    void testCreateIncorrectPerson() throws Exception {
        PersonCreateRequestDto dto = new PersonCreateRequestDto(
                "", "lastName",
                Instant.now(),"",
                UserRole.READER
        );
        mockMvc.perform(post("/api/v1/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("First name must be between 2 and 20 characters; "));
    }

    @Test
    @WithUserDetails
    void testSuccessfulCreatePerson() throws Exception {
        PersonCreateRequestDto dto = new PersonCreateRequestDto(
                "name", "lastName",
                Instant.now(),"",
                UserRole.READER
        );
        when(personMapper.toPerson(dto)).thenReturn(new Person());
        mockMvc.perform(post("/api/v1/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
    @Test
    @WithUserDetails
    void testSuccessfulGetMyUUID() throws Exception {
        mockMvc.perform(get("/api/v1/persons/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(String.valueOf(testUserDetailsService.getUserId())));
    }

    @Test
    @WithUserDetails
    void testGetMyFavoritesAuthors() throws Exception {
        when(personService.loadFavorites(any())).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/persons/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testSuccessfulGetAuthors() throws Exception {
        when(personService.loadAuthors(0, 10)).thenReturn(org.springframework.data.domain.Page.empty());
        mockMvc.perform(get("/api/v1/persons/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithUserDetails
    void testSuccessfulAddToFavorites() throws Exception {
        UUID uuid = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/persons/favorites/{uuid}", uuid))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails
    void testAddToFavoritesAlreadyFavoritePerson() throws Exception {
        UUID uuid = UUID.randomUUID();
        doThrow(new PersonAlreadyInFavoritesException())
                .when(personService).addToFavorites(testUserDetailsService.getUserId(),uuid);
        mockMvc.perform(post("/api/v1/persons/favorites/{uuid}", uuid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Person already in favorites"));
    }

    @Test
    @WithUserDetails
    void testAddToFavoritesNonExistPerson() throws Exception {
        UUID uuid = UUID.randomUUID();
        doThrow(new PersonNotFoundException())
                .when(personService).addToFavorites(testUserDetailsService.getUserId(),uuid);
        mockMvc.perform(post("/api/v1/persons/favorites/{uuid}", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person not found"));
    }

    @Test
    @WithUserDetails
    void testAddToFavoritesYourself() throws Exception {
        UUID uuid = testUserDetailsService.getUserId();
        doThrow(new SelfRequestException())
                .when(personService).addToFavorites(uuid,uuid);
        mockMvc.perform(post("/api/v1/persons/favorites/{uuid}", uuid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This is you"));
    }

    @Test
    @WithUserDetails
    void testSuccessfulRemoveFromFavorites() throws Exception {
        UUID uuid = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/persons/favorites/{uuid}", uuid))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails
    void testRemoveFromFavoritesNonExistOrNonFavoritePerson() throws Exception {
        UUID uuid = UUID.randomUUID();
        doThrow(new PersonNotFoundException())
                .when(personService).removeFromFavorites(testUserDetailsService.getUserId(),uuid);
        mockMvc.perform(delete("/api/v1/persons/favorites/{uuid}", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person not found"));
    }

    @Test
    @WithUserDetails
    void testRemoveFromFavoritesYourself() throws Exception {
        UUID uuid = testUserDetailsService.getUserId();
        doThrow(new SelfRequestException())
                .when(personService).removeFromFavorites(uuid,uuid);
        mockMvc.perform(delete("/api/v1/persons/favorites/{uuid}", uuid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This is you"));
    }

    @Test
    void testSuccessfulSearchPerson() throws Exception {
        when(personService.searchPersons("test", 0, 10)).thenReturn(org.springframework.data.domain.Page.empty());
        mockMvc.perform(get("/api/v1/persons/search")
                        .param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testSuccessfulGetPerson() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(personService.loadPersonByUUID(uuid)).thenReturn(new Person());
        when(personMapper.toDto(any(Person.class))).thenReturn(new PersonResponseDto(
                UUID.randomUUID(),"","",Instant.now(),Instant.now(),UserRole.READER
        ));
        mockMvc.perform(get("/api/v1/persons/{uuid}", uuid))
                .andExpect(status().isOk());
    }

    @Test
    void testGetNonExistPerson() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(personService.loadPersonByUUID(uuid)).thenThrow(new PersonNotFoundException());
        mockMvc.perform(get("/api/v1/persons/{uuid}", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person not found"));
    }

    @Test
    void testSuccessfulGetPersonBiography() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(personService.loadPersonBiographyByUUID(uuid)).thenReturn(new BiographyResponseDto(""));
        mockMvc.perform(get("/api/v1/persons/{uuid}/biography", uuid))
                .andExpect(status().isOk());
    }

    @Test
    void testGetNonExistPersonBiography() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(personService.loadPersonBiographyByUUID(uuid)).thenThrow(new BiographyNotFoundException());
        mockMvc.perform(get("/api/v1/persons/{uuid}/biography", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Biography not found"));
    }

    @Test
    @WithUserDetails
    void testUpdatePerson() throws Exception {
        PersonUpdateRequestDto dto = new PersonUpdateRequestDto();
        mockMvc.perform(patch("/api/v1/persons/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails
    void testUpdateIncorrectPerson() throws Exception {
        PersonUpdateRequestDto dto = new PersonUpdateRequestDto(
                "", "lastName",
                Instant.now(),"",
                UserRole.READER
        );
        mockMvc.perform(patch("/api/v1/persons/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("First name must be between 2 and 20 characters; "));
    }
}