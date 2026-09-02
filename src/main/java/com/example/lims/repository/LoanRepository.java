package com.example.lims.repository;

import com.example.lims.model.Loan;
import com.example.lims.enums.LoanStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {
    List<Loan> findByUserId(UUID userId);
    List<Loan> findByCopyId(UUID copyId);

    @Query("SELECT l FROM Loan l WHERE l.user.id = :userId AND l.status IN :statuses")
    List<Loan> findByUserIdAndStatusIn(@Param("userId") UUID userId, @Param("statuses") List<LoanStatus> statuses);

    @Query("SELECT l FROM Loan l WHERE l.status IN ('ACTIVE', 'RENEWED', 'OVERDUE') AND l.dueDate < CURRENT_DATE")
    List<Loan> findExpiredLoans();

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.user.id = :userId AND l.status IN :statuses")
    long countByUserIdAndStatusIn(UUID userId, List<LoanStatus> statuses);

    @Query(value = "SELECT li.id, li.title, " +
            "STRING_AGG(CONCAT(a.last_name, ' ', a.first_name), ', ') AS authors, " +
            "COUNT(l.id) AS cnt " +
            "FROM loans l " +
            "JOIN item_copies ic ON l.copy_id = ic.id " +
            "JOIN library_items li ON ic.item_id = li.id " +
            "LEFT JOIN item_authors ia ON li.id = ia.item_id " +
            "LEFT JOIN authors a ON ia.author_id = a.id " +
            "WHERE (:year IS NULL OR EXTRACT(YEAR FROM l.loan_date) = :year) " +
            "GROUP BY li.id, li.title " +
            "ORDER BY cnt DESC",
            nativeQuery = true)
    List<Object[]> findPopularBooksRaw(@Param("year") Integer year, Pageable pageable);

    @Query("SELECT COUNT(l) FROM Loan l")
    long countTotalLoans();

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status = 'ACTIVE' OR l.status = 'RENEWED' OR l.status = 'OVERDUE'")
    long countActiveLoans();

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.returnDate > l.dueDate")
    long countOverdueReturns();

    @Query("SELECT l.user.id, COUNT(l) FROM Loan l " + "WHERE l.status IN :statuses GROUP BY l.user.id")
    List<Object[]> countActiveLoansByUser(@Param("statuses") List<LoanStatus> statuses);
}