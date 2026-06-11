package com.library.repository;

import com.library.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByAuthorContainingIgnoreCase(String author);

    Page<Book> findByTitleContainingIgnoreCaseAndActiveTrue(String title, Pageable pageable);

    Page<Book> findByCategory(Book.BookCategory category, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0 AND b.active = true")
    List<Book> findAllAvailableBooks();

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchBooks(@Param("keyword") String keyword, Pageable pageable);

    @Modifying
    @Query("UPDATE Book b SET b.availableCopies = b.availableCopies - 1 WHERE b.id = :bookId AND b.availableCopies > 0")
    int decrementAvailableCopies(@Param("bookId") Long bookId);

    @Modifying
    @Query("UPDATE Book b SET b.availableCopies = b.availableCopies + 1 WHERE b.id = :bookId")
    int incrementAvailableCopies(@Param("bookId") Long bookId);

    long countByCategory(Book.BookCategory category);

    List<Book> findTop10ByOrderByCreatedAtDesc();
}
