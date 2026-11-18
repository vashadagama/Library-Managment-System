package com.example.lims.repository;

import com.example.lims.entity.loan.Loan;
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

    @Query("""
            select l from Loan l
            where l.user.id = :userId and l.status.name in :statuses
            """)
    List<Loan> findByUserIdAndStatusNames(@Param("userId") UUID userId,
                                          @Param("statuses") List<String> statuses);

    @Query("""
            select l from Loan l
            where l.status.name in :statuses
            order by l.dueDate asc, l.loanDate asc
            """)
    List<Loan> findByStatusNames(@Param("statuses") List<String> statuses);

    @Query("""
            select l from Loan l
            where l.status.name in :statuses and l.dueDate < current_date
            """)
    List<Loan> findExpiredLoans(@Param("statuses") List<String> statuses);

    @Query("""
            select count(l) from Loan l
            where l.user.id = :userId and l.status.name in :statuses
            """)
    long countByUserIdAndStatusNames(@Param("userId") UUID userId,
                                     @Param("statuses") List<String> statuses);

    @Query(value = """
            SELECT li.id, li.title, li_authors.authors AS authors,
                   COUNT(l.id) AS cnt
            FROM loans l
            JOIN item_copies ic ON l.copy_id = ic.id
            JOIN library_items li ON ic.item_id = li.id
            LEFT JOIN (SELECT ia.item_id,
                              STRING_AGG(CONCAT(a.last_name, ' ', a.first_name), ', ') AS authors
                       FROM item_authors ia
                       JOIN authors a ON ia.author_id = a.id
                       GROUP BY ia.item_id) li_authors ON li_authors.item_id = li.id
            WHERE (:year IS NULL OR EXTRACT(YEAR FROM l.loan_date) = :year)
            GROUP BY li.id, li.title, li_authors.authors
            ORDER BY cnt DESC
            """,
            nativeQuery = true)
    List<Object[]> findPopularBooksRaw(@Param("year") Integer year, Pageable pageable);

    @Query("SELECT COUNT(l) FROM Loan l")
    long countTotalLoans();

    @Query("""
            SELECT COUNT(l) FROM Loan l
            WHERE l.status.name in ('ACTIVE', 'RENEWED', 'OVERDUE')
            """)
    long countActiveLoans();

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.returnDate > l.dueDate")
    long countOverdueReturns();

    @Query("""
            SELECT l.user.id, COUNT(l) FROM Loan l
            WHERE l.status.name in :statuses
            GROUP BY l.user.id
            """)
    List<Object[]> countActiveLoansByUser(@Param("statuses") List<String> statuses);
}
