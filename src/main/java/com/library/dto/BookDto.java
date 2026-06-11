package com.library.dto;

import com.library.model.Book;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;  // javax → jakarta in Boot 3
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @NotBlank(message = "ISBN is required")
    private String isbn;

    private String description;

    @Min(1450)
    @Max(2100)
    private Integer publishedYear;

    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private Double price;

    @Min(0)
    private Integer availableCopies;

    private Boolean active;

    private Book.BookCategory category;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
