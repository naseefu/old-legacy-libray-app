package com.library.service;

import com.library.dto.BorrowRecordDto;
import com.library.exception.BookNotAvailableException;
import com.library.exception.BookNotFoundException;
import com.library.exception.UserNotFoundException;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;    // Log4j2 — Log4Shell era
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BorrowService {

    private static final Logger logger = LogManager.getLogger(BorrowService.class);

    private static final int DEFAULT_BORROW_DAYS = 14;
    private static final double FINE_PER_DAY = 1.50;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private ModelMapper modelMapper = new ModelMapper();

    public BorrowRecordDto borrowBook(Long bookId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + bookId));

        if (book.getAvailableCopies() <= 0) {
            throw new BookNotAvailableException("No copies available for: " + book.getTitle());
        }

        long activeBorrows = borrowRecordRepository.countByUserAndStatus(user, BorrowRecord.BorrowStatus.BORROWED);
        if (activeBorrows >= 5) {
            throw new IllegalStateException("User has reached maximum borrow limit of 5 books");
        }

        int updated = bookRepository.decrementAvailableCopies(bookId);
        if (updated == 0) {
            throw new BookNotAvailableException("Book copy was taken concurrently");
        }

        BorrowRecord record = new BorrowRecord();
        record.setBook(book);
        record.setUser(user);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(DEFAULT_BORROW_DAYS));
        record.setStatus(BorrowRecord.BorrowStatus.BORROWED);
        record.setReturned(false);

        BorrowRecord saved = borrowRecordRepository.save(record);
        logger.info("Book '{}' borrowed by user '{}', due: {}", book.getTitle(), username, record.getDueDate());
        return modelMapper.map(saved, BorrowRecordDto.class);
    }

    public BorrowRecordDto returnBook(Long recordId, String username) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new BookNotFoundException("Borrow record not found: " + recordId));

        if (!record.getUser().getUsername().equals(username)) {
            throw new SecurityException("This borrow record doesn't belong to user: " + username);
        }

        record.setReturnDate(LocalDate.now());
        record.setReturned(true);
        record.setStatus(BorrowRecord.BorrowStatus.RETURNED);

        if (LocalDate.now().isAfter(record.getDueDate())) {
            long daysOverdue = record.getDueDate().until(LocalDate.now()).getDays();
            record.setFineAmount(daysOverdue * FINE_PER_DAY);
            logger.warn("Book '{}' returned {} days overdue. Fine: ${}", 
                        record.getBook().getTitle(), daysOverdue, record.getFineAmount());
        }

        bookRepository.incrementAvailableCopies(record.getBook().getId());
        BorrowRecord saved = borrowRecordRepository.save(record);
        return modelMapper.map(saved, BorrowRecordDto.class);
    }

    public Page<BorrowRecordDto> getUserBorrowHistory(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        return borrowRecordRepository.findByUser(user, PageRequest.of(page, size))
                .map(r -> modelMapper.map(r, BorrowRecordDto.class));
    }

    // Scheduled task — runs every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void markOverdueRecords() {
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findOverdueRecords(LocalDate.now());
        logger.info("Found {} overdue records", overdueRecords.size());

        List<BorrowRecord> updated = overdueRecords.stream().peek(record -> {
            record.setStatus(BorrowRecord.BorrowStatus.OVERDUE);
            long daysOverdue = record.getDueDate().until(LocalDate.now()).getDays();
            record.setFineAmount(daysOverdue * FINE_PER_DAY);
        }).collect(Collectors.toList());

        borrowRecordRepository.saveAll(updated);
    }
}
