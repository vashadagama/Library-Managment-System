package main.java.com.vasha.lims.repository;

import java.util.ArrayList;
import java.util.List;
import main.java.com.vasha.lims.enums.LoanStatus;
import main.java.com.vasha.lims.model.LibraryItem;
import main.java.com.vasha.lims.model.Loan;
import main.java.com.vasha.lims.model.User;


public class LoanRepository {
    private final List<Loan> loans = new ArrayList<>();

    public void save(Loan loan) {
        if (loan != null && !loans.contains(loan)) {
            loans.add(loan);
        }
    }

    public List<Loan> findLoansByUser(User user) {
        List<Loan> userLoans = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.getUser().equals((user))) {
                userLoans.add(loan);
            }
        }
        return userLoans;
    }

    public boolean hasActiveLoanForItem(User user, LibraryItem item) {
        for (Loan loan : findLoansByUser(user)) {
            if (loan.getItem().equals(item) &&
               (loan.getStatus() == LoanStatus.ACTIVE || loan.getStatus() == LoanStatus.RENEWED)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasOverdueLoans(User user) {
        for (Loan loan : findLoansByUser(user)) {
            loan.updateStatusIfOverdue();
            if (loan.getStatus() == LoanStatus.OVERDUE) {
                return true;
            }
        }
        return false;
    }
}