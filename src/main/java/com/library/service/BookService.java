package com.library.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;      // Guava
import com.google.common.collect.Maps;               // Guava
import com.library.dto.BookDto;
import com.library.exception.BookNotFoundException;
import com.library.exception.DuplicateIsbnException;
import com.library.model.Book;
import com.library.repository.BookRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;              // log4j2 Logger (Log4Shell era)
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Book Service
 *
 * Uses:
 * - Log4j2 API (Log4Shell vulnerable version)
 * - Guava ImmutableList, Maps (fine but old pattern)
 * - ModelMapper 2.3.x (some config API changed in 3.x)
 * - Jackson ObjectMapper (old configuration style)
 * - PageRequest.of() — same in new versions (this part is fine)
 */
@Service
@Transactional
public class BookService {

    // Log4j2 Logger — Log4Shell vulnerability in log4j-core 2.14.0
    private static final Logger logger = LogManager.getLogger(BookService.class);

    @Autowired
    private BookRepository bookRepository;

    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    private ObjectMapper objectMapper;

    @Cacheable(value = "books", key = "#id")
    public BookDto getBookById(Long id) {
        logger.info("Fetching book with id: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
        return modelMapper.map(book, BookDto.class);
    }

    @Cacheable(value = "books", key = "'all-page-' + #page + '-' + #size")
    public Page<BookDto> getAllBooks(int page, int size) {
        // Sort.by(Sort.Direction, String) — fine, same API
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return bookRepository.findAll(pageRequest)
                .map(book -> modelMapper.map(book, BookDto.class));
    }

    public BookDto createBook(BookDto bookDto) {
        logger.info("Creating new book with ISBN: {}", bookDto.getIsbn());

        if (bookRepository.findByIsbn(bookDto.getIsbn()).isPresent()) {
            throw new DuplicateIsbnException("Book with ISBN " + bookDto.getIsbn() + " already exists");
        }

        Book book = modelMapper.map(bookDto, Book.class);
        Book saved = bookRepository.save(book);
        logger.info("Book created successfully with id: {}", saved.getId());
        return modelMapper.map(saved, BookDto.class);
    }

    @CachePut(value = "books", key = "#id")
    public BookDto updateBook(Long id, BookDto bookDto) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));

        // Old style: manually map fields
        if (StringUtils.isNotBlank(bookDto.getTitle())) {
            existing.setTitle(bookDto.getTitle());
        }
        if (StringUtils.isNotBlank(bookDto.getAuthor())) {
            existing.setAuthor(bookDto.getAuthor());
        }
        if (bookDto.getPrice() != null) {
            existing.setPrice(bookDto.getPrice());
        }

        Book updated = bookRepository.save(existing);
        return modelMapper.map(updated, BookDto.class);
    }

    @CacheEvict(value = "books", key = "#id")
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }

    public Page<BookDto> searchBooks(String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return bookRepository.searchBooks(keyword, pageRequest)
                .map(book -> modelMapper.map(book, BookDto.class));
    }

    public List<BookDto> getAvailableBooks() {
        List<Book> books = bookRepository.findAllAvailableBooks();
        // Using Guava ImmutableList — fine, but old pattern
        ImmutableList<Book> immutableBooks = ImmutableList.copyOf(books);
        return immutableBooks.stream()
                .map(book -> modelMapper.map(book, BookDto.class))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getBookStatistics() {
        // Guava Maps.newHashMap() — works but old style
        Map<String, Object> stats = Maps.newHashMap();
        stats.put("totalBooks", bookRepository.count());
        stats.put("availableBooks", bookRepository.findAllAvailableBooks().size());
        for (Book.BookCategory category : Book.BookCategory.values()) {
            stats.put("category_" + category.name(), bookRepository.countByCategory(category));
        }
        return stats;
    }

    /**
     * Serialize book to JSON using ObjectMapper.
     * Old style: no modules registered explicitly.
     * In newer Jackson, JavaTimeModule auto-registration differs.
     */
    public String serializeBook(BookDto bookDto) {
        try {
            return objectMapper.writeValueAsString(bookDto);
        } catch (Exception e) {
            logger.error("Failed to serialize book: {}", e.getMessage());
            return "{}";
        }
    }
}
