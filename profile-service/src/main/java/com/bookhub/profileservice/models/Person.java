package com.bookhub.profileservice.models;

import com.bookhub.profileservice.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "persons")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Person {
    @Id
    private UUID id;

    private String firstName;

    private String lastName;

    private Instant dateOfBirth;

    @CreatedDate
    private Instant dateOfRegistration;

    private String biography;

    @Enumerated(value = EnumType.STRING)
    private UserRole role;

}
