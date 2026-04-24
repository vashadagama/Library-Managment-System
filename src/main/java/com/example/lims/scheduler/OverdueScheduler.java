package com.example.lims.scheduler;

import com.example.lims.enums.LoanStatus;
import com.example.lims.model.Loan;
import com.example.lims.repository.LoanRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OverdueScheduler {

    private final LoanRepository loanRepository;

    public OverdueScheduler(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void markOverdueLoans() {
        List<Loan> expiredLoans = loanRepository.findExpiredLoans();
        for (Loan loan : expiredLoans) {
            loan.setStatus(LoanStatus.OVERDUE);
        }
        loanRepository.saveAll(expiredLoans);
        if (!expiredLoans.isEmpty()) {
            System.out.println("Обновлён статус OVERDUE для " + expiredLoans.size() + " выдач.");
        }
    }
}