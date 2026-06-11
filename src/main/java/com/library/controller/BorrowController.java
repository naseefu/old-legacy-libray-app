package com.library.controller;

import com.library.dto.BorrowRecordDto;
import com.library.service.BorrowService;
import com.library.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @PostMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BorrowRecordDto>> borrowBook(
            @PathVariable Long bookId,
            Authentication authentication) {
        String username = authentication.getName();
        BorrowRecordDto record = borrowService.borrowBook(bookId, username);
        return ResponseEntity.ok(ApiResponse.success(record));
    }

    @PutMapping("/return/{recordId}")
    public ResponseEntity<ApiResponse<BorrowRecordDto>> returnBook(
            @PathVariable Long recordId,
            Authentication authentication) {
        String username = authentication.getName();
        BorrowRecordDto record = borrowService.returnBook(recordId, username);
        return ResponseEntity.ok(ApiResponse.success(record));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<BorrowRecordDto>>> getHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String username = authentication.getName();
        Page<BorrowRecordDto> history = borrowService.getUserBorrowHistory(username, page, size);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
