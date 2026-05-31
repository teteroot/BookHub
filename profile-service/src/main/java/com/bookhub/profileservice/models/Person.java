package com.bookhub.profileservice.models;

import com.bookhub.profileservice.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "persons")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "person_favorites",
            joinColumns = @JoinColumn(name = "marked_as_favorite_by_id"),
            inverseJoinColumns = @JoinColumn(name = "favorite_author_id")
    )
    private Set<Person> favoriteAuthors;

    @ManyToMany(mappedBy = "favoriteAuthors", fetch = FetchType.LAZY)
    private Set<Person> markedAsFavoriteBy;

}
