package com.bookhub.profileservice.controllers;

import com.bookhub.profileservice.config.SecurityConfig;
import com.bookhub.profileservice.config.properties.SecurityOriginProperties;
import com.bookhub.profileservice.dtos.requests.PersonCreateRequestDto;
import com.bookhub.profileservice.dtos.requests.PersonUpdateRequestDto;
import com.bookhub.profileservice.dtos.responses.BiographyResponseDto;
import com.bookhub.profileservice.dtos.responses.PersonResponseDto;
import com.bookhub.profileservice.enums.UserRole;
import com.bookhub.profileservice.exceptions.extensions.BiographyNotFoundException;
import com.bookhub.profileservice.exceptions.extensions.PersonNotFoundException;
import com.bookhub.profileservice.mappers.PersonMapper;
import com.bookhub.profileservice.models.Person;
import com.bookhub.profileservice.security.TestUserDetailsService;
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
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PersonController.class)
@Import({SecurityConfig.class, TestUserDetailsService.class})
class PersonControllerTest {

    @MockitoBean
    private PersonMapper personMapper;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private SecurityOriginProperties securityOriginProperties;

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