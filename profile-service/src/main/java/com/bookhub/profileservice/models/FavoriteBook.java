package com.bookhub.profileservice.models;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "favorite_books",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"person_id", "book_id"}
        )
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FavoriteBook {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;


    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @CreationTimestamp
    private Instant createdAt;

}



