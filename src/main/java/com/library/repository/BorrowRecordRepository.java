package com.library.repository;

import com.library.model.BorrowRecord;
import com.library.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    Page<BorrowRecord> findByUser(User user, Pageable pageable);

    List<BorrowRecord> findByUserAndStatus(User user, BorrowRecord.BorrowStatus status);

    @Query("SELECT br FROM BorrowRecord br WHERE br.dueDate < :today AND br.status = 'BORROWED'")
    List<BorrowRecord> findOverdueRecords(@Param("today") LocalDate today);

    long countByUserAndStatus(User user, BorrowRecord.BorrowStatus status);
}
