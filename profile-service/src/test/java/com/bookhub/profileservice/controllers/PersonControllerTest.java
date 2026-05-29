package com.bookhub.profileservice.controllers;

import com.bookhub.profileservice.dtos.requests.PersonCreateRequestDto;
import com.bookhub.profileservice.enums.UserRole;
import com.bookhub.profileservice.mappers.PersonMapper;
import com.bookhub.profileservice.models.Person;
import com.bookhub.profileservice.services.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PersonController.class)
class PersonControllerTest {

    @MockitoBean
    private PersonMapper personMapper;

    @MockitoBean
    private PersonService personService;

    @Autowired
    private WebApplicationContext webApplicationContext;

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
                null,
                "name", "lastName",
                Instant.now(),"",
                UserRole.READER
        );
        mockMvc.perform(post("/api/v1/persons/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID must not be null; "));
    }

    @Test
    void testSuccessfulCreatePerson() throws Exception {
        PersonCreateRequestDto dto = new PersonCreateRequestDto(
                UUID.randomUUID(),
                "name", "lastName",
                Instant.now(),"",
                UserRole.READER
        );
        when(personMapper.toPerson(any(PersonCreateRequestDto.class))).thenReturn(new Person());
        mockMvc.perform(post("/api/v1/persons/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
}