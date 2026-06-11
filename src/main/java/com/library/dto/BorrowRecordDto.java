package com.library.dto;

import com.library.model.BorrowRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordDto {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String username;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Boolean returned;
    private BorrowRecord.BorrowStatus status;
    private Double fineAmount;
    private LocalDateTime createdAt;
}
