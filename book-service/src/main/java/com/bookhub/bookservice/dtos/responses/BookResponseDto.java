package com.bookhub.bookservice.dtos.responses;

import com.bookhub.bookservice.enums.BookStatus;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link com.bookhub.bookservice.models.Book}
 */
public record BookResponseDto(UUID id, String title, String description, Integer ageLimit, UUID authorId,
                              BookStatus status, Instant dateOfPublishing, Integer countOfPages, String s3ArchivePath,
                              String s3CoverPath) implements Serializable {
}