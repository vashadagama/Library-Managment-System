package com.example.lims.repository;

import com.example.lims.model.Loan;
import com.example.lims.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {
    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findByUserId(UUID userId);
    
    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.dueDate < :today")
    List<Loan> findExpiredLoans(LocalDate today);
}