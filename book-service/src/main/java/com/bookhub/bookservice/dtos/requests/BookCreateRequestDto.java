package com.bookhub.bookservice.dtos.requests;

import java.io.Serializable;

/**
 * DTO for {@link com.bookhub.bookservice.models.Book}
 */
public record BookCreateRequestDto(String title,String description, Integer ageLimit) implements Serializable{
}