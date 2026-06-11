package com.library;

import com.library.dto.BookDto;
import com.library.model.Book;
import com.library.repository.BookRepository;
import com.library.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration Test
 *
 * Note: @SpringBootTest with full context will fail at startup in Boot 3
 * because of broken Springfox Swagger2 @EnableSwagger2 annotation.
 * Fix: Remove Springfox entirely before running tests in Boot 3.
 */
@SpringBootTest
@ActiveProfiles("test")
class LibraryApplicationTests {

    @Autowired
    private BookService bookService;

    @MockBean
    private BookRepository bookRepository;

    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = new Book();
        sampleBook.setId(1L);
        sampleBook.setTitle("Test Book");
        sampleBook.setAuthor("Test Author");
        sampleBook.setIsbn("9780000000001");
        sampleBook.setPrice(19.99);
        sampleBook.setAvailableCopies(3);
        sampleBook.setActive(true);
        sampleBook.setCategory(Book.BookCategory.FICTION);
    }

    @Test
    @WithMockUser(roles = "USER")
    void contextLoads() {
        assertNotNull(bookService);
    }

    @Test
    void getBookById_ShouldReturnBook_WhenExists() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        BookDto result = bookService.getBookById(1L);

        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        assertEquals("Test Author", result.getAuthor());
    }

    @Test
    void getBookById_ShouldThrowException_WhenNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(com.library.exception.BookNotFoundException.class,
                () -> bookService.getBookById(99L));
    }
}
