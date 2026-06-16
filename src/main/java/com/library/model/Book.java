package com.library.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;    // Removed/changed in Hibernate 6
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;              // javax.persistence.* → jakarta.persistence.* in Spring Boot 3
import javax.validation.constraints.*;  // javax.validation.* → jakarta.validation.* in Spring Boot 3
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Book Entity
 *
 * BREAKS on upgrade:
 * - javax.persistence.* → jakarta.persistence.* (Spring Boot 3 / Jakarta EE 9+)
 * - javax.validation.* → jakarta.validation.* (Spring Boot 3)
 * - @org.hibernate.annotations.Type(type="...") API changed in Hibernate 6
 *   e.g., @Type(type="yes_no") → @JdbcTypeCode(SqlTypes.CHAR) or @Convert
 * - @Cache usage still works but package changed in Hibernate 6
 */
@Entity
@Table(name = "books", indexes = {
    @Index(name = "idx_book_isbn", columnList = "isbn", unique = true),
    @Index(name = "idx_book_title", columnList = "title")
})
@EntityListeners(AuditingEntityListener.class)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    @Column(nullable = false)
    @NotBlank(message = "Author is required")
    private String author;

    @Column(unique = true, nullable = false, length = 13)
    @NotBlank
    @Pattern(regexp = "^(?:ISBN(?:-13)?:? )?(?=[0-9]{13}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)")
    private String isbn;

    @Column(length = 2000)
    private String description;

    @Column(name = "published_year")
    @Min(1450)
    @Max(2100)
    private Integer publishedYear;

    @Column(nullable = false)
    @DecimalMin(value = "0.0")
    private Double price;

    @Column(name = "available_copies")
    @Min(0)
    private Integer availableCopies = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookCategory category;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(
        name = "book_tags",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BorrowRecord> borrowRecords = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum BookCategory {
        FICTION, NON_FICTION, SCIENCE, HISTORY, TECHNOLOGY, BIOGRAPHY, CHILDREN, MYSTERY, ROMANCE
    }
}
