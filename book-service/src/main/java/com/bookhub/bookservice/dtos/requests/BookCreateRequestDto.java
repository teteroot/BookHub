package com.bookhub.bookservice.dtos.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for {@link com.bookhub.bookservice.models.Book}
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BookCreateRequestDto {

    @NotBlank(message = "Title is required and cannot be blank")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Min(value = 0, message = "Age limit cannot be negative")
    @Max(value = 100, message = "Age limit cannot exceed 100")
    private Integer ageLimit;

}