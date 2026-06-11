package com.library.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.BookDto;
import com.library.service.BookService;
import com.library.util.ApiResponse;   // springfox — REMOVED in Spring Boot 3

/**
 * Book Controller
 *
 * BREAKS on Spring Boot 3 upgrade:
 * - io.swagger.annotations.* (springfox) → io.swagger.v3.oas.annotations.* (springdoc)
 * - @Api → @Tag
 * - @ApiOperation → @Operation
 * - @ApiParam → @Parameter
 * - javax.validation.* → jakarta.validation.*
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping // springfox
    public ResponseEntity<ApiResponse<Page<BookDto>>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookDto> books = bookService.getAllBooks(page, size);
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @GetMapping("/{id}")// springfox
    public ResponseEntity<ApiResponse<BookDto>> getBookById(
            @PathVariable Long id) {  // springfox @ApiParam
        BookDto book = bookService.getBookById(id);
        return ResponseEntity.ok(ApiResponse.success(book));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookDto>> createBook(
           @RequestBody BookDto bookDto) {   // @Valid from javax → jakarta
        BookDto created = bookService.createBook(bookDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookDto>> updateBook(
            @PathVariable Long id,
            @RequestBody BookDto bookDto) {
        BookDto updated = bookService.updateBook(id, bookDto);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BookDto>>> searchBooks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookDto> results = bookService.searchBooks(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<?>> getAvailableBooks() {
        return ResponseEntity.ok(ApiResponse.success(bookService.getAvailableBooks()));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        return ResponseEntity.ok(ApiResponse.success(bookService.getBookStatistics()));
    }
}
