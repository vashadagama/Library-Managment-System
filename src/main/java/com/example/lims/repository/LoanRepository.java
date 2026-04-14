package com.example.lims.repository;

import com.example.lims.model.Loan;
import com.example.lims.enums.LoanStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {
    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findByUserId(UUID userId);
    List<Loan> findByCopyId(UUID copyId);

    @Query("SELECT l FROM Loan l WHERE l.user.id = :userId AND l.status IN :statuses")
    List<Loan> findByUserIdAndStatusIn(@Param("userId") UUID userId, @Param("statuses") List<LoanStatus> statuses);

    @Query("SELECT l FROM Loan l WHERE l.status IN ('ACTIVE', 'RENEWED', 'OVERDUE') AND l.dueDate < :today")
    List<Loan> findExpiredLoans(LocalDate today);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.user.id = :userId AND l.status IN :statuses")
    long countByUserIdAndStatusIn(UUID userId, List<LoanStatus> statuses);

    @Query("SELECT l.copy.item.id, l.copy.item.title, a.lastName, a.firstName, COUNT(l) as cnt " +
            "FROM Loan l JOIN l.copy.item.authors a " +
            "WHERE (:year IS NULL OR YEAR(l.loanDate) = :year) " +
            "GROUP BY l.copy.item.id, l.copy.item.title, a.lastName, a.firstName " +
            "ORDER BY cnt DESC")
    List<Object[]> findPopularBooksRaw(@Param("year") Integer year, Pageable pageable);

    @Query("SELECT COUNT(l) FROM Loan l")
    long countTotalLoans();

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status = 'ACTIVE' OR l.status = 'RENEWED' OR l.status = 'OVERDUE'")
    long countActiveLoans();

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.returnDate > l.dueDate")
    long countOverdueReturns();
}